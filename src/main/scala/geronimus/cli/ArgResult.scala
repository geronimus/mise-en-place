package geronimus.cli

/** Contains the result of evaluating an argument against a defined parameter
  * `geronimus.cli.Setting`.
  * @param found Whether or not the parameter's name or alias was found amongst
  *   the arguments.
  * @param valued Whether or not any values were found following the parameter
  *   name or alias in the arguments.
  * @param valid Any and all arguments, found following the parameter name or
  *   alias, that match any of its `geronimus.cli.Setting`'s defined options
  *   (if any), and passed its validator functions (if any).
  * @param invalid Any and all arguments, found following the parameter name or
  *   alias, that do not match any of its `geronimus.cli.Setting`'s defined
  *   options (if any), or failed its validator functions (if any).
  */
final case class ArgResult(
  found : Boolean,
  valued : Boolean,
  valid : Seq[ String ],
  invalid : Seq[ String ]
) {
  override def toString =
    s"${ this.getClass.getSimpleName }(found = ${ found }, " +
      s"valued = ${ valued }, valid = ${ valid }, invalid = ${ invalid })"
}

object ArgResult {

  /** The completely empty ArgResult object. This is what
    * `geronimus.cli.ArgSet` returns, when the user asks it for setting names
    * or aliases not defined as parameters.
    */
  val empty = ArgResult(
    found = false,
    valued = false,
    valid = Vector.empty,
    invalid = Vector.empty
  )
}

