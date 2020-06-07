package geronimus.util

import org.scalatest.funspec.AnyFunSpec
import scala.util.Random

class Sha1Spec extends AnyFunSpec {

  describe( "sha1( bytes : Array[ Byte ] )" ) {

    it( "Returns the empty hash for null input." ) {
      assert( sha1( null ) == sha1( Array.empty[ Byte ] ) )  
    }

    it( "Returns the same hash for identical values." ) {
      assert(
        sha1( "...of a reflection".getBytes( "UTF-8" ) ) ==
          sha1( "...of a reflection".getBytes( "UTF-8" ) )
      )  
    }
  
    it( "Produces distinct hexidecimal Strings for randomized input bytes." ) {
      
      val inputs = ( 1 to 10 )
        .map {
          _ => ( Random.alphanumeric take ( Random.between( 3, 21 ) ) ).mkString
      }
      val results : Set[ String ] = inputs.map( in => sha1( in.getBytes( "UTF-8" ) ) )
        .toSet

      assert( inputs.size == results.size )
      
      results.foreach {
        result => assert( result matches "^[0-9a-f]{40}$" )  
      }
    }
  }
}

