package geronimus.cli

import org.scalatest.funspec.AnyFunSpec

class DefaultSpec extends AnyFunSpec {

  describe( "Setting.setDefault( value : String )" ) {

    it( "The default value cannot contain line break or shell characters." ) {
    
      val badVals = (
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

      badVals.foreach {
        badVal => assertThrows[ IllegalArgumentException ] {
          rejector.setDefault( badVal )
        }
      }
    }
  }
}

