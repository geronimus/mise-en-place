package geronimus.error

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers._

class IllegalArgSpec extends AnyFunSpec {

  describe( "StdError.illegalArg( parameter : String, expected : String, found : Any[, explanation : String ] )" ) {

    val parameter = "testValue"
    val expected = "Describe the legal value."
    val found = "An literal illegal value, or its String representation."
    val explanation = "An optional, additional message explaining the error."

    it( "Thows an exception of type IllegalArgumentException" ) {
      
      assertThrows[ IllegalArgumentException ]{
        StdError.illegalArg( parameter, expected, found, explanation )  
      }
    }

    it( "Passing null to any parameter does not change the type of exception." ) {
      
      assertThrows[ IllegalArgumentException ]{
        StdError.illegalArg( null, expected, found, explanation )  
      }
      
      assertThrows[ IllegalArgumentException ]{
        StdError.illegalArg( parameter, null, found, explanation )  
      }
      
      assertThrows[ IllegalArgumentException ]{
        StdError.illegalArg( parameter, expected, null, explanation )  
      }
      
      assertThrows[ IllegalArgumentException ]{
        StdError.illegalArg( parameter, expected, found, null )  
      }
    }

    it( "Its error message contains all of the text values you pass in." ) {
    
      val error = the [ IllegalArgumentException ] thrownBy {  
        StdError.illegalArg( parameter, expected, found, explanation )  
      }

      assert( error.getMessage.contains( parameter ) )
      assert( error.getMessage.contains( expected ) )
      assert( error.getMessage.contains( found ) )
      assert( error.getMessage.contains( explanation ) )
      assert( error.getMessage.count( _ == '\n' ) == 5 )
    }

    it( "The explanation (additional comment) is optional." ) {  
    
      val error = the [ IllegalArgumentException ] thrownBy {  
        StdError.illegalArg( parameter, expected, found )  
      }

      assert( error.getMessage.contains( parameter ) )
      assert( error.getMessage.contains( expected ) )
      assert( error.getMessage.contains( found ) )
      assert( !error.getMessage.contains( explanation ) )
      assert( error.getMessage.count( _ == '\n' ) == 3 )
    }

    it(
      "We limit output text to 510 characters. (Paranoid security feature.)"
    ) {

      def randomString( length : Int ) =
        ( scala.util.Random.alphanumeric take length ).mkString
    
      val tooLongParam = randomString( 511 )
      val acceptedParam = tooLongParam take 510

      val tooLongExpected = randomString( 511 )
      val acceptedExpected = tooLongExpected take 510

      val tooLongFound = randomString( 511 )
      val acceptedFound = tooLongFound take 510
      
      val tooLongExplanation = randomString( 511 )
      val acceptedExplanation = tooLongExplanation take 510

      val error = the [ IllegalArgumentException ] thrownBy {
        StdError.illegalArg(
          tooLongParam,
          tooLongExpected,
          tooLongFound,
          tooLongExplanation
        )  
      }

      assert( !error.getMessage.contains( tooLongParam ) )
      assert( error.getMessage.contains( acceptedParam ) )

      assert( !error.getMessage.contains( tooLongExpected ) )
      assert( error.getMessage.contains( acceptedExpected ) )
      
      assert( !error.getMessage.contains( tooLongFound ) )
      assert( error.getMessage.contains( acceptedFound ) )
      
      assert( !error.getMessage.contains( tooLongExplanation ) )
      assert( error.getMessage.contains( acceptedExplanation ) )
    }
  }
}

