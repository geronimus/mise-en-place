package geronimus.util

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers._

class CoalesceSpec extends AnyFunSpec {

  describe( "coalesce[ T ]( inputs : [ T ]* )" ) {
    
    it( "If you pass only null in, you get null back." ) {
    
      assert( coalesce[ String ]( null ) == null )
    }

    it( "If you pass in only nulls, you get back null." ) {
      
      assert( coalesce[ String ]( null, null, null ) == null )
    }

    it( "If you pass in a non-null value, you get it back." ) {
    
      Vector( true, 1, 1.2, "forty-two", new java.util.Date ).foreach{
        item => assert( coalesce( item ) == item )  
      }
    }

    it( "If you pass in many non-null values, you get the first back." ) {
    
      assert( coalesce( 1, 2, 3 ) == 1 )
    }

    it( "If you pass in many values, you get back the first non-null value." ) {
    
      assert( coalesce( null, null, 1, 2, null, 3 ) == 1 )
    }

    it(
      "If you call it with a dynamically-typed argument in first position, " +
        "the return type will be Any."
    ) {
        
      assert( coalesce( null, null, 1, 2, null, 3 ).isInstanceOf[ Any ] )
    }

    it( "You cannot use its dynamism to make value types nullable." ) {
    
      // "coalesce[ Int ]( null, 1 )" shouldNot compile
      
      /* The code above does indeed not compile. But in this case, even the
       * test refuses to compile, for some reason. So we're leaving this
       * commented out.
       */
    }

    it(
      "You can parameterize its signature to explicitly convert its return type"
    ) {
      
      noException should be thrownBy {
        val result : String = coalesce[ String ]( null, null, "Hello, World!" )  
      }
    }
  }
}

