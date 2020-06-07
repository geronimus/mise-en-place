package geronimus.cli

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers._

class SettingDefineSpec extends AnyFunSpec {

  describe( "Setting.define( name : String )" ) {
  
    it( "You can define a new setting with just its name." ) {
    
      noException should be thrownBy {
        Setting.define( "config" )  
      }
    }

    it(
      "Names must be between 1 and 255 characters long, and " +
        "they cannot contain whitespace, or shell characters." +
        "(Since this object is meant to resolve command-line arguments.)"
    ) {
    
      val badNames = (
        Vector(
          null,
          "",
          ( scala.util.Random.alphanumeric take 256 ).mkString
        ) ++
          "`#$%&*(){}[]<>\"'".map(
            illegalChar => s"bad${ illegalChar }name"
          ) ++
          "\u00A0 \r\n".map( space => s"bad${ space }name" )
      )
      
      val rejector = Setting.define( "rejector" )

      badNames.foreach {
        badName => assertThrows[ IllegalArgumentException ] {
          rejector.addAlias( badName )
        }  
      }
    }

    it( "A new setting has default values for its required properties." ) {
    
      val settingName = "config"
      val setting = Setting.define( settingName )

      assert( setting.name == settingName )
      assert( setting.getRequired == false )
      assert( !setting.hasDefault )
      assert( setting.getDefault == None )
      assert( setting.getDescription == setting.name )
      assert( setting.getErrorText == s"Bad value for setting: ${ setting.name }" )
      assert( setting.getAliases == Set.empty )
      assert( setting.getOptions == Set.empty )
      assert( setting.getValidators == Set.empty )
    }
  }

  describe(
    "Setting.define(\n" +
      "  name : String,\n" + 
      "  required : Boolean = false,\n" +
      "  default : String = null,\n" +
      "  description : String = null,\n" +
      "  errorText : String = null,\n" +
      "  aliases : Set[ String ] = Set.empty,\n" +
      "  options : Set[ String ] = Set.empty,\n" +
      "  validators : Set[ String => Boolean ] = Set.empty\n" +
      ")"
  ) {
  
    it( "If you want, you can create a Setting in one go." ) {
      
      val name = "host"
      val required = true
      val default = "localhost"
      val desc = "The DNS name where we can find the job controller."
      val error = "Could locate a host using the DNS name provided."
      val aliases = Set( "ServerName", "h" )
      val opts = Set( "localhost", "127.0.0.1", "192.168.0.0" )
      val validators = Set(
        ( value : String  ) => {
          value == "localhost" || ( value matches "\\d{1,3}(\\.\\d{1,3}){3}" )
        },
        ( value : String ) => {
          value == "localhost" ||
            ( value take 4 ) == "127." ||
            ( value take 8 ) == "192\\.168\\." 
        }
      )

      val host = Setting.define(
        name = name,
        required = required,
        default = default,
        description = desc,
        errorText = error,
        aliases = aliases,
        options = opts,
        validators = validators
      )

      assert( host.name == name )
      assert( host.getRequired )
      assert( host.hasDefault )
      assert( host.getDefault == Some( default ) )
      assert( host.getDescription == desc )
      assert( host.getErrorText == error )
      assert( host.getOptions == opts )
      assert( host.getValidators == validators )
    }

    it( "Apart from name, all other arguments are optional" ) {
      
      val name = "sparseHost"
      val validators = Set(
        ( value : String  ) => {
          value == "localhost" || ( value matches "\\d{1,3}(\\.\\d{1,3}){3}" )
        }
      )

      val sparseHost = Setting.define( name = name, validators = validators )

      assert( sparseHost.name == name )
      assert( !sparseHost.getRequired )
      assert( !sparseHost.hasDefault )
      assert( sparseHost.getDescription == name )
      assert( sparseHost.getErrorText == s"Bad value for setting: ${ name }" )
      assert( sparseHost.getAliases == Set.empty )
      assert( sparseHost.getOptions == Set.empty )
      assert( sparseHost.getValidators == validators )
    }
  }
}

