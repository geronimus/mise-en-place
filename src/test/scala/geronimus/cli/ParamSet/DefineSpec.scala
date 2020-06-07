package geronimus.cli

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers._

class ParamSetDefineSpec extends AnyFunSpec {

  describe( "ParamSet" ) {
  
    it( "You cannot define one with null." ) {
      assertThrows[ IllegalArgumentException ] { ParamSet.define( null ) }
    }

    it( "Nor can you define one with an empty sequence." ) {
      assertThrows[ IllegalArgumentException ] { ParamSet.define( Seq.empty ) }
    }

    it( "You must define one with a sequence of Setting objects." ) {
      noException should be thrownBy {
        ParamSet.define( Vector( Setting.define( "host" ) ) )  
      }
    }

    it( "The Settings cannot have the same name" ) {
    
      val host = Setting.define( "host" )
      val host2 = Setting.define( "host" )

      assertThrows[ IllegalArgumentException ] {
        ParamSet.define( Vector( host, host2 ) )
      }
    }

    it( "Setting names cannot be duplicated in aliases either." ) {
    
      val host = Setting.define( name = "host", aliases = Set( "h" ) )
      val help = Setting.define( name = "help", aliases = Set( "h" ) )

      assertThrows[ IllegalArgumentException ]{
        ParamSet.define( Vector( host, help ) )  
      }

      val server = Setting.define( "Server" )
      val webServer =
        Setting.define( name = "WebServer", aliases = Set( "Server" ) )

      assertThrows[ IllegalArgumentException ] {
        ParamSet.define( Vector( server, webServer ) )  
      }

      val nameNode =
        Setting.define( name = "NameNode", aliases = Set( "Node" ) )
      val dataNode =
        Setting.define( name = "DataNode", aliases = Set( "Node" ) )

      assertThrows[ IllegalArgumentException ] {
        ParamSet.define( Vector( nameNode, dataNode ) )
      }
    }
  }
}

