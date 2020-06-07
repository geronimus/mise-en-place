package geronimus.cli

import geronimus.error.StdError.illegalArg
import geronimus.util.coalesce

/** A factory for Settings definitions.
  */
object Setting {

  private val illegalChars = "`#$%&*(){}[]<>\"'"

  private val legalIdDesc = "A text string, between 1 and 255 characters " +
    "long, without spaces or line breaks, and with none of the following " +
    "characters: ( " + illegalChars.mkString( ", " ) + ")"

  private val legalOptionDesc = "A text string, between 1 and 255 characters " +
    "long, without line breaks, and with none of the following characters: ( " +
    illegalChars.mkString( ", " ) + ")"

  /** Synonym for `define`. Define a new Setting object, whose properties you
    * can configure later.
    * @param name A Setting's principal identifier. Must be between 1 and 255
    *   characters, and cannot be null. You cannot change it, once it is defined.
    */
  def apply( name : String ) : Setting = define( name )

  /** Synonym for `define`. Define a new Setting with the option to configure
    * all of its properties at once.
    * @param name A Setting's principal identifier. Must be between 1 and 255
    *   characters, and cannot be null. You cannot change it, once it is defined.
    * @param required Indicates whether or not a value is required for this
    *   Setting.
    * @param default The default value that will be used for this Setting, if
    *   none is provided.
    * @param description Describes the Setting to the user, for example, in
    *   help messages.
    * @param errorText The text to display when an invalid value is received
    *   for this setting. If you provide no value, its value defaults to:
    *   "Bad value for setting: " + the Setting's name.
    * @param aliases The Set of other names (eg, short names) that can be used,
    *   on the command line, instead of this Setting's full name.
    * @param options The Set of possible values that this Setting can take.
    * @param validators A Set of functions that can be used to determine
    *   whether the value passed to this Setting is allowed.
    */
  def apply(
    name : String,
    required : Boolean = false,
    default : String = null,
    description : String = null,
    errorText : String = null,
    aliases : Set[ String ] = Set.empty,
    options : Set[ String ] = Set.empty,
    validators : Set[ String => Boolean ] = Set.empty
  ) : Setting = define(
    name,
    required,
    default,
    description,
    errorText,
    aliases,
    options,
    validators
  )
  
  /** Define a new Setting object, whose properties you can configure later.
    * @param name A Setting's principal identifier. Must be between 1 and 255
    *   characters, and cannot be null. You cannot change it, once it is defined.
    */
  def define( name : String ) : Setting = {
    validateName( name )
    new Setting( name )
  }

  /** Define a new Setting with the option to configure all of its properties
    * at once.
    * @param name A Setting's principal identifier. Must be between 1 and 255
    *   characters, and cannot be null. You cannot change it, once it is defined.
    * @param required Indicates whether or not a value is required for this
    *   Setting.
    * @param default The default value that will be used for this Setting, if
    *   none is provided.
    * @param description Describes the Setting to the user, for example, in
    *   help messages.
    * @param errorText The text to display when an invalid value is received
    *   for this setting. If you provide no value, its value defaults to:
    *   "Bad value for setting: " + the Setting's name.
    * @param aliases The Set of other names (eg, short names) that can be used,
    *   on the command line, instead of this Setting's full name.
    * @param options The Set of possible values that this Setting can take.
    * @param validators A Set of functions that can be used to determine
    *   whether the value passed to this Setting is allowed.
    */
  def define(
    name : String,
    required : Boolean = false,
    default : String = null,
    description : String = null,
    errorText : String = null,
    aliases : Set[ String ] = Set.empty,
    options : Set[ String ] = Set.empty,
    validators : Set[ String => Boolean ] = Set.empty
  ) : Setting = {

    val setting = Setting.define( name )

    setting.setRequired( required )
    if ( default != null ) setting.setDefault( default )
    if ( description != null ) setting.setDescription( description )
    if ( errorText != null ) setting.setErrorText( errorText )
    setting.addAliases( aliases )
    setting.addOptions( options )
    setting.addValidators( validators )

    setting
  }

  private def containsControlChar( text : String ) =
    text.exists( char => char < 32 )

  private def containsIllegalChar( text : String ) =
    text.exists( char => illegalChars.contains( char ) )

  private def containsLinebreak( text : String ) =
    text.exists( char => "\r\n".contains( char ) )

  private def containsSpace( text : String ) =
    text.exists( char => " \u00A0".contains( char ) )

  private def containsWhitespace( text : String ) =
    containsSpace( text ) || containsLinebreak( text )

  private def isEmpty( text : String ) =
    text == null || text == ""

  private def isLegalIdentifier( id : String ) =
    !isEmpty( id ) &&
      id.length < 256 &&
      !containsControlChar( id ) &&
      !containsWhitespace( id ) &&
      !containsIllegalChar( id )

  private def validateName( name : String ) =
    if ( !isLegalIdentifier( name ) )
      illegalArg(
        parameter = "name",
        expected = legalIdDesc,
        found = name
      )
}

/** A data stucture representing a parameter, whose value we expect the user
  * to provide. (eg, Via a command-line argument.) Describes the information
  * that you expect to receive, and helps you to constrain its legal values.
  */
class Setting private ( val name : String ) {

  private var aliases = Set.empty[ String ]
  private var default : Option[ String ] = None
  private var description = name
  private var errorText = s"Bad value for setting: $name"
  private var options = Set.empty[ String ]
  private var required = false
  private var validators = Set.empty[ String => Boolean ]

  /** Add an alternate name for this Setting.
    */
  def addAlias( value : String ) = {
    validateAliasName( value )
    if ( value != name )
      aliases = aliases + value
  }

  /** Add a Set of alternate names for this Setting.
    */
  def addAliases( values : Set[ String ] ) = values.foreach {
    value => addAlias( value )
  }

  /** Add a legal value to this Setting's collection of defined legal values.
    * Use options when there is a known range of acceptable options.
    * @param value The string representation of a legal value for this Setting.
    */
  def addOption( value : String ) = {
    validateOption( value )
    options = options + value
  }

  /** Add a Set of defined legal values for this Setting.
    * Use options when there is a known range of acceptable options.
    * @param values A Set of strings representing this Setting's legal values.
    */
  def addOptions( values : Set[ String ] ) = values.foreach {
    value => addOption( value )
  }

  /** Add a function that validates whether a received value is allowed or not.
    * A setting can have multiple validation functions.
    * Use validators when a Setting's legal values can't be expressed using a
    * short list of allowed options.
    * @param value A function that returns true if the argument is valid, and
    *   false otherwise.
    */
  def addValidator( value : String => Boolean ) = {
    validators = validators + value  
  }

  /** Add a Set of functions that validate whether a received value is allowed
    * or not. A setting can have multiple validation functions.
    * Use validators when a Setting's legal values can't be expressed using a
    * short list of allowed options.
    * @param values A Set of functions that return true if the argument is
    *   valid, and false otherwise.
    */
  def addValidators( values : Set[ String => Boolean ] ) = {
    validators = validators ++ values  
  }

  /** The alternate identifiers for this Setting, in addition to its name.
    */
  def getAliases = aliases

  /** The value that this Setting will take when no value is assigned.
    * @return Some[ String ] if a default has been assigned. Otherwise None.
    */
  def getDefault = default

  /** The explanation of this Setting to show to users, in documentation and in
    * help messages. If not defined, this will be the Setting's name.
    */
  def getDescription = description

  /** The text shown to users when this Setting receives an invalid value. If
    * you don't define this, the message will be: "Bad value for setting: ",
    * followed by the Setting's name.
    */
  def getErrorText = errorText

  /** The Set of defined legal values for this Setting.
    */
  def getOptions = options

  /** Indicates whether or not defining a value for this Setting is mandatory.
    * New Settings are created with this value set to false.
    */
  def getRequired = required

  /** The Set of functions that validate whether a received value is allowed or
    * not. A setting can have multiple validation functions.
    * Use validators when a Setting's legal values can't be expressed using a
    * short list of allowed options.
    */
  def getValidators = validators

  /** Indicates whether or not a default value has been defined for this Setting.
    */
  def hasDefault = default != None

  /** Define the value that this Setting will take when no value is assigned.
    */
  def setDefault( value : String ) = {
    validateOption( value )
    default = Some( value )
  }

  /** Define the explanation of this Setting to show to users, in documentation
    * and in help messages. If not defined, this will be the Setting's name.
    */
  def setDescription( value : String ) = {
    
    description = coalesce( value, name ).asInstanceOf[ String ]
  }

  /** Define the text shown to users when this Setting receives an invalid
    * value. If you don't define this, the message will be: "Bad value for
    * setting: ", followed by the Setting's name.
    */
  def setErrorText( value : String ) = { errorText = value }

  /** Define whether or not defining a value for this Setting is mandatory.
    * New Settings are created with this value set to false.
    */
  def setRequired( value : Boolean ) = { required = value }

  /** A String representation of this Setting.
    */
  override def toString =
    s"${ this.getClass.getSimpleName }(name = ${ name }, " +
      s"required = ${ required }, default = ${ default }, " +
      s"description = ${ description }, errorText = ${ errorText }, " +
      s"aliases = ${ aliases }, options = ${ options }, " +
      s"validators = ${ validators })"

  private def isLegalOption( value : String ) =
    !Setting.isEmpty( value ) &&
      value.length < 256 &&
      !Setting.containsControlChar( value ) &&
      !Setting.containsLinebreak( value ) &&
      !Setting.containsIllegalChar( value )

  private def validateAliasName( alias : String ) =
    if ( !Setting.isLegalIdentifier( alias ) )
      illegalArg(
        parameter = "alias",
        expected = Setting.legalIdDesc,
        found = alias
      )

  private def validateDefault( value : String ) =
    validateValue( value, "default" )

  private def validateOption( value : String ) =
    validateValue( value, "option" )

  private def validateValue( value : String, param : String ) =
    if ( !isLegalOption( value ) )
      illegalArg(
        parameter = param,
        expected = Setting.legalOptionDesc,
        found = value
      )
}

