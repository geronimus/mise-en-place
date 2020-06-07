package geronimus.cli

import org.scalatest.funspec.AnyFunSpec

class SettingPropertiesSpec extends AnyFunSpec {

  describe( "Setting" ) {
    
    it( "You can change its default values to suit your requirements." ) {
    
      val host = Setting.define( "host" )

      val aliases = Set( "ServerName", "h" )
      host.addAliases( aliases )
      assert( host.getAliases == aliases )

      host.setRequired( true )
      assert( host.getRequired == true )

      val default = "localhost"
      host.setDefault( default )
      assert( host.hasDefault )
      assert( host.getDefault == Some( default ) )

      val desc = "The DNS name where we can find the job controller."
      host.setDescription( desc )
      assert( host.getDescription == desc )

      val error = "Could locate a host using the DNS name provided."
      host.setErrorText( error )
      assert( host.getErrorText == error )

      val opts = Set( "localhost", "127.0.0.1", "192.168.0.0" )
      host.addOptions( opts )
      assert( host.getOptions == opts )

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
      host.addValidators( validators )
      assert( host.getValidators == validators )
    }

    it( "You can add aliases, options, and validators one-by-one as well." ) {
    
      val host = Setting.define( "host" )

      val serverAlias = "ServerName"
      val hAlias = "h"

      List( serverAlias, hAlias ).foreach {
        alias => host.addAlias( alias )  
      }
      assert( host.getAliases == Set( serverAlias, hAlias ) )
      
      val localOpt = "localhost"
      val loopbackOpt = "127.0.0.1"
      val cClassOpt = "192.168.0.0"

      List( localOpt, loopbackOpt, cClassOpt ).foreach {
        option => host.addOption( option )  
      }
      assert( host.getOptions == Set( localOpt, loopbackOpt, cClassOpt ) )

      val formatValidator = ( value : String  ) => {
          value == "localhost" || ( value matches "\\d{1,3}(\\.\\d{1,3}){3}" )
      }
      val contentValidator = ( value : String ) => {
        value == "localhost" ||
          ( value take 4 ) == "127." ||
          ( value take 8 ) == "192\\.168\\." 
      }

      List( formatValidator, contentValidator ).foreach {
        validator => host.addValidator( validator )  
      }
      assert( host.getValidators == Set( formatValidator, contentValidator ) )
    }

  }
}

