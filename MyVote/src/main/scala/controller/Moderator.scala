package controller

import java.util.{ArrayList, Date}
import scala.annotation.meta.beanGetter
import scala.beans.BeanProperty
import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.constraints.NotNull

/**
 * @author Dhwani Shah
 */

class Moderator (
            @JsonProperty("email") @BeanProperty @(NotNull @beanGetter) var email:String,
            @JsonProperty("password") @BeanProperty @(NotNull @beanGetter) var password:String,
            @JsonProperty("name") @BeanProperty @(NotNull @beanGetter) var name:String) {

            var id:Int=0
            var created_at:Date=null
            var updated_at:Date=null
            var pollList:ArrayList[Poll]=new ArrayList[Poll]()

            def setId(id : Int)={
              this.id = id
            }
            def getId():Int={
              return this.id
            }

            def setCreatedAt(created_at : Date)={
              this.created_at = created_at
            }
            def getCreatedAt() :Date={
              return this.created_at
            }
            
            def setUpdatedAt(updated_at : Date)={
              this.updated_at = updated_at
            }
            def getUpdatedAt() :Date={
              return this.updated_at
            }

            def setPollList(pollList : ArrayList[Poll])={
              this.pollList = pollList
            }
            def getPollList() : ArrayList[Poll]={
              return this.pollList
            }
}