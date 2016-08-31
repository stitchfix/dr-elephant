package com.linkedin.drelephant.util


import fastparse.all._
import java.util.{Map => JMap}
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

/**
 *   The Java arg parsing code is broken, so make simple PEG parser
 *   
 *   Need to handle both -Dvar=val type declarations ,
 *   as well as -XX: System parametes
 */
object ParamParser extends  {
  
  
    lazy val allowedChars = CharIn( 'a' to 'z') | CharIn( 'A' to 'Z') | CharIn( '0' to '9') | CharIn(Seq('.'))
    lazy val specialChars = CharIn( Seq(':', '.' , '/', '-', '+', '%')  )
    lazy val javaParam = P( allowedChars.rep.! )
    lazy val unquotedValue = P( (allowedChars | specialChars).rep.! )
    lazy val whiteSpace = CharIn(Seq(' ', '\t'))
    
    lazy val quotedValue = P( "'" ~ ( allowedChars| specialChars | whiteSpace).rep.!  ~ "'")
    
    lazy val javaParamValue = quotedValue | unquotedValue 
    
    lazy val javaParamDecl = P( "-D" ~ javaParam ~ "=" ~ javaParamValue )
    
    /// For 
    lazy val sysFlagDecl = P( "-XX:" ~ CharIn( Seq( '+', '-') ).! ~ javaParam ).map {
        case (plMin,param) =>{
          plMin match {
            case "+" => (param, "true")
            case "-" => (param, "false")
          }
          
        }
    }
        
    lazy val sysParamDecl = P( "-XX:" ~ javaParam ~ "=" ~javaParamValue )
    
    
    lazy val paramDecl = javaParamDecl | sysParamDecl | sysFlagDecl

    lazy val javaOptionsParser = Start ~ whiteSpace.rep(min=0) ~ (paramDecl ~ whiteSpace.rep ).rep ~ whiteSpace.rep(min=0) ~ End

    def parseJavaParam( txt : String) : Array[String] = {
      val paramTuple : Tuple2[String,String] = paramDecl.parse( txt ) match {
        case Parsed.Success( tup, idx ) => tup
        case fail : Parsed.Failure =>  throw new IllegalArgumentException(s" Can't parse ${txt} ")
      }
      Array( paramTuple._1, paramTuple._2 )
    }
    
    
    def parseJavaOptions( txt : String) : JMap[String,String] = {
       val tupSeq :Seq[(String,String)] = javaOptionsParser.parse(txt) match {
          case Parsed.Success( tupSeq, idx ) =>tupSeq
          case fail : Parsed.Failure =>  throw new IllegalArgumentException(s" Can't parse ${txt} ")
      }
 
      mapAsJavaMap(tupSeq.toMap)
    }
    

}