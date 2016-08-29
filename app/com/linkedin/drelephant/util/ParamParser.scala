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
  
  
    val allowedChars = CharIn( 'a' to 'z') | CharIn( 'A' to 'Z') | CharIn( '0' to '9') | CharIn(Seq('.'))
    val specialChars = CharIn( Seq(':', '.' , '/', '-', '+', '%')  )
    val javaParam = P( allowedChars.rep.! )
    val unquotedValue = P( (allowedChars | specialChars).rep.! )
    val whiteSpace = CharIn(Seq(' ', '\t'))
    
    val quotedValue = P( "'" ~ ( allowedChars| specialChars | whiteSpace).rep.!  ~ "'")
    
    val javaParamValue = quotedValue | unquotedValue 
    
    val javaParamDecl = P( "-D" ~ javaParam ~ "=" ~ javaParamValue )
    
    /// For 
    val sysFlagDecl = P( "-XX:" ~ CharIn( Seq( '+', '-') ).! ~ javaParam ).map {
        case (plMin,param) =>{
          plMin match {
            case "+" => (param, "true")
            case "-" => (param, "false")
          }
          
        }
    }
        
    val sysParamDecl = P( "-XX:" ~ javaParam ~ "=" ~javaParamValue )
    
    
    val paramDecl = javaParamDecl | sysParamDecl | sysFlagDecl

    val javaOptionsParser = Start ~ whiteSpace.rep(min=0) ~ (paramDecl ~ whiteSpace.rep ).rep ~ whiteSpace.rep(min=0) ~ End

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