package geronimus.cli

/** Holds the data returned when you call
  * `geronimus.cli.ParamSet.parseArgs( args : Array[ String ] )`. For each
  * defined parameter setting, it contains a `geronimus.cli.ArgResult` object
  * that reports on whether or not the argument was found and validated.
  * @param extraArgs Any arguments found that could not be associated with a
  * defined parameter setting.
  */
final class ArgSet (
  resultMap : Map[ Setting, ArgResult ],
  val extraArgs : Vector[ String ] = Vector.empty
) {

  /** Returns `true` if no arguments matched a defined parameter setting. */
  lazy val isEmpty = resultMap.filter(
    keyValTuple => keyValTuple._2.found
  ).size == 0

  /** Returns `true` if all required parameters are satisfied with at least one
    * valid argument.
    */
  lazy val hasRequired = missingRequiredParams.isEmpty

  /** The set containing the names of all defined parameter settings. */
  lazy val keySet = params.keySet

  /** The set containing all parameter Setting objects defined as required, but
    * where no valid value was found.
    */
  lazy val missingRequiredParams = (
    for {
      keyValTuple <- resultMap
        if keyValTuple._1.getRequired && keyValTuple._2.valid.isEmpty
    }
    yield ( keyValTuple._1 )
  ).toSet

  /** A Map of all parameter setting names to their definition objects. */
  lazy val params  =
    for ( keyValTuple <- resultMap )
    yield ( keyValTuple._1.name -> keyValTuple._1 )

  /** A Map of all parameter setting names, and their aliases, to their
    * definition objects.
    */
  lazy val paramsLookup = params ++ {
    for {
      keyValTuple <- params
      alias <- keyValTuple._2.getAliases
    } yield ( alias -> keyValTuple._2 )
  }

  /** A Map of all parameter setting names to their ArgResult report objects.
    */
  lazy val results =
    for ( keyValTuple <- resultMap )
    yield ( keyValTuple._1.name -> keyValTuple._2 )
  
  /** A Map of all parameter setting names, and their aliases, to their
    * ArgResult report objects.
    */
  lazy val resultsLookup = results ++ {
    for {
      keyValTuple <- resultMap
      alias <- keyValTuple._1.getAliases
    } yield ( alias -> keyValTuple._2 )
  }

  /** Retrieves the `geronimus.cli.ArgResult` object associated with the name
    * or alias you provide. If you use a name or alias that isn't defined as
    * a parameter, then you will get an empty `geronimus.cli.ArgResult`.
    * @param key The name or alias of a defined parameter setting.
    */
  def apply( key : String ) =
    if ( resultsLookup.keySet.contains( key ) )
      resultsLookup( key )
    else
      ArgResult(
        found = false,
        valued = false,
        valid = Vector.empty,
        invalid = Vector.empty
      )

  /** Reports whether or not this ArgSet contains a defined parameter with the
    * provided name or alias.
    */
  def hasParam( key : String ) = paramsLookup.keySet.contains( key )

  /** Retrieves the `geronimus.cli.ArgResult` object associated with the name
    * or alias you provide. If you use a name or alias that isn't defined as
    * a parameter, then you will get an empty `geronimus.cli.ArgResult`.
    * @param key The name or alias of a defined parameter setting.
    */
  def result( key : String ) = apply( key )

  override def toString = this.getClass.getSimpleName +
    results.toString.substring( 3 )
}

