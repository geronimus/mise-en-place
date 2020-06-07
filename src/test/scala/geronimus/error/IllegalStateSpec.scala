package geronimus.error

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers._

class IllegalStateSpec extends AnyFunSpec {

  describe( "StdError.illegalState( source : String, rule : String[, violation : String ] )" ) {
  
    val source = "geronimus.error.StdError"
    val rule = "This function produces illegal state exceptions."
    val violation = "You triggered an illegal state exception."

    it( "Throws an exception of type IllegalStateException." ) {
      
      assertThrows[ IllegalStateException ]{
        StdError.illegalState( source, rule, violation )  
      }
    }

    it( "Passing null to any parameter does not change the type of exception." ) {
      
      assertThrows[ IllegalStateException ]{
        StdError.illegalState( null, rule, violation )  
      }
      
      assertThrows[ IllegalStateException ]{
        StdError.illegalState( source, null, violation )  
      }
      
      assertThrows[ IllegalStateException ]{
        StdError.illegalState( source, rule, null )  
      }
    }

    it( "Its error message contains all of the text values you pass in." ) {
    
      val error = the [ IllegalStateException ] thrownBy {  
        StdError.illegalState( source, rule, violation )  
      }

      assert( error.getMessage.contains( source ) )
      assert( error.getMessage.contains( rule ) )
      assert( error.getMessage.contains( violation ) )
      assert( error.getMessage.count( _ == '\n' ) == 3 )
    }

    it( "The violation (what you did wrong) is optional." ) {  
    
      val error = the [ IllegalStateException ] thrownBy {  
        StdError.illegalState( source, rule )  
      }

      assert( error.getMessage.contains( source ) )
      assert( error.getMessage.contains( rule ) )
      assert( error.getMessage.count( _ == '\n' ) == 2 )
    }

    it(
      "We limit output text to 510 characters. (Paranoid security feature.)"
    ) {

      def randomString( length : Int ) =
        ( scala.util.Random.alphanumeric take length ).mkString
    
      val tooLongSource = randomString( 511 )
      val acceptedSource = tooLongSource take 510

      val tooLongRule = randomString( 511 )
      val acceptedRule = tooLongRule take 510

      val tooLongViolation = randomString( 511 )
      val acceptedViolation = tooLongViolation take 510

      val error = the [ IllegalStateException ] thrownBy {
        StdError.illegalState(
          tooLongSource,
          tooLongRule,
          tooLongViolation
        )  
      }

      assert( !error.getMessage.contains( tooLongSource ) )
      assert( error.getMessage.contains( acceptedSource ) )

      assert( !error.getMessage.contains( tooLongRule ) )
      assert( error.getMessage.contains( acceptedRule ) )
      
      assert( !error.getMessage.contains( tooLongViolation ) )
      assert( error.getMessage.contains( acceptedViolation ) )
    }
  }

  describe( "StdError.illegalState( source : Object, rule : String[, violation : String ] )" ) {
    
    val rule = "This variation allows an object as source."
    val violation = "You triggered an illegal state exception."

    it( "An override allows an object reference to be the source argument." ) {
    
      val error = the [ IllegalStateException ] thrownBy {  
        StdError.illegalState( this, rule, violation )  
      }

      assert( error.getMessage.contains( this.getClass.getName ) )
      assert( error.getMessage.contains( rule ) )
      assert( error.getMessage.contains( violation ) )
    }
  }
}

