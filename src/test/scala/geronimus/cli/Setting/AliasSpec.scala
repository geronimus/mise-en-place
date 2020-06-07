package geronimus.cli

import org.scalatest.funspec.AnyFunSpec

class SettingAliasSpec extends AnyFunSpec {

  describe(
    "Setting.addAlias( value : String ) / .addAliases( values Seq[ String ] )"
  ) {
  
    it( "Aliases cannot contain characters illegal in Setting names." ) {

      val badAliases = (
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

      badAliases.foreach {
        badAlias => assertThrows[ IllegalArgumentException ] {
          rejector.addAlias( badAlias )
        }  
      }

      assertThrows[ IllegalArgumentException ] {
        rejector.addAliases( badAliases.toSet )  
      }
    }

    it( "Aliases equal to the Setting name get ignored." ) {
      
      val jacob = Setting.define( "jacob" )
      jacob.addAlias( "jacob" )
      assert( jacob.getAliases == Set.empty )
    }
  }
}

