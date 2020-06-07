package geronimus.cli

import org.scalatest.funspec.AnyFunSpec

class ArgSetRequiredSpec extends AnyFunSpec {
      
  val validateUnixPath =
    ( arg : String ) => ( arg matches raw"\.?(/[a-zA-Z0-9 _\-\.]+)+" )
  // At this time, I'm thinking we need a better interface for "define",
  // which I start to use below.
  val config = Setting(
    name = "--configPath",
    required = true,
    validators = Set( validateUnixPath )
  )
  val out = Setting(
    name = "--output",
    required = true,
    validators = Set( validateUnixPath )
  )
  val verbose = Setting( "--verbose" )
  val reqParams = ParamSet( config, out, verbose )

  describe( "ArgSet - Required Parameters" ) {

    it( "Will tell you when no required args are missing" ) {
      
      val argSet = reqParams.parseArgs(
        Array( "--configPath", "./here", "--output", "./there" )
      )
      assert( argSet.hasRequired )
      assert( argSet.missingRequiredParams.isEmpty )
    }
  
    it( "Will tell you when any required args are missing." ) {

      val argSet = reqParams.parseArgs( Array( "--output", "./here", "--verbose" ) )
      assert( !argSet.hasRequired )
      assert( argSet.missingRequiredParams == Set( config ) )
    }

    it( "Will tell you when any required args are missing, due to being invalid." ) {
    
      val argSet = reqParams.parseArgs(
        Array( "--configPath", "...", "--output", "..." )
      )
      assert( !argSet.hasRequired )
      assert( argSet.missingRequiredParams == Set( config, out ) )
    }

    it( "When there is a default for a missed required argument, it gets substituted." ) {
    
      val logging = Setting(
        name = "--logs",
        required = true,
        options = Set( "on", "off" ),
        default = "off"
      )
      val logParam = ParamSet( logging )
      val logArgSet = logParam.parseArgs( Array.empty[ String ] )
      val logArg = logArgSet( "--logs" )

      assert( logArgSet.missingRequiredParams.isEmpty )
      assert( !logArg.found )
      assert( logArg.valued )
      assert( logArg.valid == Seq( "off" ) )
      assert( logArg.invalid.isEmpty )
    }

    it( "When there is a default for an invalid required argument, it does not get substituted." ) {
    
      val logging = Setting(
        name = "--logs",
        required = true,
        options = Set( "on", "off" ),
        default = "off"
      )
      val logParam = ParamSet( logging )
      val logArgSet = logParam.parseArgs( Array( "--logs", "heisenburg state" ) )
      val logArg = logArgSet( "--logs" )

      assert( logArg.found )
      assert( logArg.valued )
      assert( logArg.valid.isEmpty )
      assert( logArg.invalid == Seq( "heisenburg state" ) )
    }
  }
}

