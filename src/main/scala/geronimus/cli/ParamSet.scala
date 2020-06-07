package geronimus.cli

import geronimus.error.StdError.illegalArg

/** Allows you to define a ParamSet object, as a collection of
  * `geronimus.cli.Setting`s.
  */
object ParamSet {

  /** Given a varargs-style sequence of `geronimus.cli.Setting`s, with mutually
    * exclusive names and aliases, this function constructs and returns a
    * ParamSet instance, capable of parsing argument arrays.
    */
  def apply( settings : Setting* ) = define( settings )

  /** Given a sequence of `geronimus.cli.Setting`s, with mutually exclusive
    * names and aliases, this function constructs and returns a ParamSet
    * instance, capable of parsing argument arrays.
    */
  def define( settings : Seq[ Setting ] ) =
    if ( settings == null || settings == Seq.empty )
      illegalArg(
        parameter = "settings",
        expected = "A sequence containing at least one Setting object.",
        found = settings
      )
    else if ( hasDuplicateNames( settings ) )
      illegalArg(
        parameter = "settings",
        expected =
          "A sequence of settings containing no duplicate names or aliases.",
        found = settings
      )
    else new ParamSet( settings )

  private def hasDuplicateNames( settings : Seq[ Setting ] ) = {
    val names =
      settings.flatMap( setting => setting.name :: setting.getAliases.toList )
    names.size != names.toSet.size
  }
}

/** A collection of `geronimus.cli.Setting` objects that can parse and validate
  * arguments arrays.
  */
class ParamSet private ( settings : Seq[ Setting ] ) {

  /** A Map of defined parameter setting names to their definition objects.
    */
  val params = {
    for ( setting <- settings )
    yield ( setting.name, setting )
  }.toMap

  /** Given an array of String arguments, produces a `geronimus.cli.ArgSet`
    * object, containing a `geronimus.cli.ArgResult` report on the arguments
    * found for each defined parameter.`
    */
  def parseArgs( args : Array[ String ] ) = {

    // Organizes args by the setting name that precedes them. Works even if
    // the args are preceded by an alias for the setting.
    // It is an Option so that None can be used to collect any args without a
    // preceding setting name or alias.
    val sortedArgs : Map[ Option[ String ], Vector[ String ] ] =
      sortArgs( args, params.values )

    val extraArgs = sortedArgs( None )

    val resultsMap : Map[ Setting, ArgResult ] = {
      for ( setting <- settings )
      yield {
        if ( sortedArgs.contains( Some( setting.name ) ) )
          setting -> makeFoundArgResult(
            setting,
            sortedArgs( Some( setting.name ) )
          )
        else
          setting -> makeNotFoundArgResult( setting )
      }
    }.toMap

    new ArgSet( resultsMap, extraArgs )
  }

  override def toString = this.getClass.getSimpleName +
    params.toString.substring( 3 )

  private def sortArgs(
    args : Array[ String ],
    settings : Iterable[ Setting ]
  ) : Map[ Option[ String ], Vector[ String ] ] = {
    
    val paramsIndex = settings.flatMap {
      setting => Map( setting.name -> setting ) ++
        setting.getAliases.map( alias => Tuple2( alias, setting ) )
    }.toMap
    var foundParam : Option[ String ] = None
    var sorter : Map[ Option[ String ], Vector[ String ] ] = Map(
      None -> Vector.empty[ String ]
    )

    // We're going to traverse the array once, and for each arg...
    args.foreach {
      arg => {
        // If it matches a param - or its alias - then we're going to make that
        // param the key for which we're currently collecting values...
        // (But until we encounter a declared param, we're collecting values for
        // the key "None".)
        if ( paramsIndex.keySet.contains( arg ) ) {
          // Then change the key for which we're currently collecting values...
          foundParam = Some( paramsIndex( arg ).name )
          // And add it, with no values (yet), to the map of found results...
          sorter = sorter + Tuple2( foundParam, Vector.empty )
        }
        else {
          // Otherwise, we'll append our value to whatever's currently in the
          // sorter for that key. If nothing, then we'll just append it to an
          // empty collection...
          val foundArgs = sorter( foundParam ) :+ arg
          // And then we update the sorter with the new value.
          sorter = sorter + Tuple2( foundParam, foundArgs )
        }
      }
    }
    sorter
  }

  private def makeFoundArgResult( setting : Setting, argVals : Vector[ String ] ) =
    if ( argVals.isEmpty && !setting.hasDefault )
      ArgResult(
        found = true,
        valued = false,
        valid = argVals,
        invalid = argVals
      )
    else if ( argVals.isEmpty && setting.hasDefault ) {
      val validation = validateArgs( setting, Vector( setting.getDefault.get ) )

      ArgResult(
        found = true,
        valued = false,
        valid = validation.passed,
        invalid = validation.failed
      )
    }
    else {
      val validation = validateArgs( setting, argVals )
      
      ArgResult(
        found = true,
        valued = true,
        valid = validation.passed,
        invalid = validation.failed
      )
    }

  private def makeNotFoundArgResult( setting : Setting ) =
    if ( setting.hasDefault ) {
      val validation = validateArgs( setting, Vector( setting.getDefault.get ) )
      ArgResult(
        found = false,
        valued = true,
        valid = validation.passed,
        invalid = validation.failed
      )
    }
    else
      ArgResult.empty

  private def validateArgs( setting : Setting, argVals : Vector[ String ] ) = {

    val validators = if ( !setting.getOptions.isEmpty )
      setting.getValidators +
        { arg : String => setting.getOptions.contains( arg ) }
    else setting.getValidators

    def isValid(
      arg : String,
      validators : Set[ String => Boolean ]
    ) : Boolean  =
      if ( validators.isEmpty ) true
      else if ( !validators.head( arg ) ) false
      else isValid( arg, validators.tail )

    var passed = Vector.empty[ String ]
    var failed = Vector.empty[ String ]

    argVals.foreach {
      arg => if ( isValid( arg, validators ) ) {
        passed = passed :+ arg  
      } else {
        failed = failed :+ arg  
      }
    }

    ValidationResult( passed, failed )
  }

  private case class ValidationResult(
    passed : Vector[ String ],
    failed : Vector[ String ]
  )
}

