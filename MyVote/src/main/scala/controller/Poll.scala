package controller

import java.util.Date
import scala.annotation.meta.beanGetter
import scala.beans.BeanProperty
import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.constraints.NotNull
import java.util.Formatter.DateTime
import java.util.ArrayList

/**
 * @author Dhwani Shah
 */

class Poll (
      @JsonProperty("question") @BeanProperty @(NotNull @beanGetter) var question:String,
      @JsonProperty("expired_at") @BeanProperty @(NotNull @beanGetter) var expired_at:Date,
      @JsonProperty("started_at") @BeanProperty @(NotNull @beanGetter) var started_at:Date,
      @JsonProperty("choice") @BeanProperty @(NotNull @beanGetter) var choice:ArrayList[String]) {
  
      var id:String=null
      var results:ArrayList[Integer] = new ArrayList[Integer]

      def setId(id : String)={
        this.id = id
      }
      def getId():String={
        return this. id
      }
  
      def setResults(results : ArrayList[Integer])={
        this.results = results
      }
      def getResults() : ArrayList[Integer]={
        return this.results
      }
}