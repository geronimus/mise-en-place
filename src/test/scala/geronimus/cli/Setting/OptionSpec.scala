package geronimus.cli

import org.scalatest.funspec.AnyFunSpec

class OptionSpec extends AnyFunSpec {

  describe(
    "Setting.addOption( value : String ) / .addOption( values Seq[ String ] )"
  ) {

    it( "Options cannot contain line break or shell characters." ) {
    
      val badOptions = (
        Vector(
          null,
          "",
          ( scala.util.Random.alphanumeric take 256 ).mkString
        ) ++
          "`#$%&*(){}[]<>\"'".map(
            illegalChar => s"bad${ illegalChar }value"
          ) ++
          "\r\n".map( space => s"bad${ space }value" )
      )
      
      val rejector = Setting.define( "rejector" )

      badOptions.foreach {
        badOption => assertThrows[ IllegalArgumentException ] {
          rejector.addOption( badOption )
        }  
      }

      assertThrows[ IllegalArgumentException ] {
        rejector.addOptions( badOptions.toSet )  
      }
    }
  }
}

