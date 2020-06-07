package geronimus.cli

import org.scalatest.funspec.AnyFunSpec

class ArgSetDefaultSpec extends AnyFunSpec {

  describe( "ArgSet - Behaviour of Defaults" ) {
  
    val clearCache = Setting(
      name = "--clear-cache",
      default = "false"
    )
    val clearCacheParam = ParamSet( clearCache )

    it( "When an argument is provided, the default value does not appear." ) {
    
      val argSet = clearCacheParam.parseArgs( Array( "--clear-cache", "true" ) )
      assert( argSet( "--clear-cache" ).valid == Seq( "true" ) )
    }

    it( "When an argument is not provided, then the default does appear." ) {
    
      val argSet = clearCacheParam.parseArgs( Array.empty[ String ] )
      val cacheArg = argSet( "--clear-cache" )

      assert( !cacheArg.found )
      assert( cacheArg.valued )
      assert( cacheArg.valid == Seq( "false" ) )
      assert( cacheArg.invalid.isEmpty )
    }

    it( "When you provide the argument key, but miss its value, the default gets used." ) {
    
      val argSet = clearCacheParam.parseArgs( Array( "--clear-cache" ) )
      val cacheArg = argSet( "--clear-cache" )

      assert( cacheArg.found )
      assert( !cacheArg.valued )
      assert( cacheArg.valid == Seq( "false" ) )
      assert( cacheArg.invalid.isEmpty )
    }

    it( "When you provide an invalid default, it gets substituted, but invalidated." ) {
    
      val logging = Setting(
        name = "--logs",
        options = Set( "on", "off" ),
        default = "heisenburg state"
      )
      val logParams = ParamSet( logging )
      val argSet = logParams.parseArgs( Array.empty[ String ] )
      val logsArg = argSet( "--logs" )

      assert( !logsArg.found )
      assert( logsArg.valued )
      assert( logsArg.valid.isEmpty )
      assert( logsArg.invalid == Seq( "heisenburg state" ) )
    }

    it( "When you provide a valid default, and an invalid argument, the default does not get used." ) {
    
      val logging = Setting(
        name = "--logs",
        options = Set( "on", "off" ),
        default = "off"
      )
      val logParams = ParamSet( logging )
      val argSet = logParams.parseArgs( Array( "--logs", "heisenburg state" ) )
      val logsArg = argSet( "--logs" )

      assert( logsArg.found )
      assert( logsArg.valued )
      assert( logsArg.valid.isEmpty )
      assert( logsArg.invalid == Seq( "heisenburg state" ) )
    }
  }
}

