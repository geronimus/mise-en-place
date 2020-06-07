package geronimus.error

import geronimus.util.coalesce

/** A little object to help standardize the production of common errors,
  * with structured, informative messages.
  */
object StdError {

  /** Throws an IllegalArgumentException with a structured, informative message.
    *
    * @param parameter Identify the parameter where the illegal value was found,
    *   from the user's point of view.
    * @param expected Describe the acceptable range of values.
    * @param found Provide or describe the argument, as is was found.
    * @param explanation Optionally provide further context or information about
    *   why the value is illegal.
    */
  def illegalArg(
    parameter : String,
    expected : String,
    found : Any,
    explanation : String = ""
  ) = {
    val message = "Illegal Argument\n  " +
      s"Parameter: ${ sanitize( parameter ) }\n  " +
      s"Expected: ${ sanitize( expected ) }\n  " +
      s"Found: ${ sanitize( coalesce( found, "null" ).toString ) }" + {
        if ( explanation == null || explanation == "" ) ""
        else "\n\n" + sanitize( explanation )
      }

    throw new IllegalArgumentException( message )
  }

  /** Throws an IllegalStateException with a structured, informative message.
    *
    * @param source Identify the object or area of the program where the rule
    *   was broken.
    * @param rule Describe the rule that makes the attempted operation illegal.
    * @param violation Optionally, provide more context or information about
    *   why this constitutes a violation of the rule.
    */
  def illegalState(
    source : String,
    rule : String,
    violation : String = ""
  ) = {
    val message = "Illegal State\n" +
      s"Source: ${ sanitize( source ) }\n  " +
      s"Rule: ${ sanitize( rule ) }" + {
        if ( violation == null || violation == "" ) ""
        else s"\n  What you did wrong: ${ sanitize( violation ) }"
      }
  
    throw new IllegalStateException( message )
  }

  /** Throws an IllegalStateException with a structured, informative message.
    *
    * @param source Identify where in the code the rule was broken, using an
    *   object reference. eg, This is an ideal place to pass a `this` reference.
    * @param rule Describe the rule that makes the attempted operation illegal.
    * @param violation Optionally, provide more context or information about
    *   why this constitutes a violation of the rule.
    */
  def illegalState(
    source : AnyRef,
    rule : String,
    violation : String
  ) : Unit =
    illegalState( source.getClass.getName, rule, violation )

  private def sanitize( str : String ) =
    if ( str == null ) "null"
    else str take 510
}

