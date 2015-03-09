package controller

import java.io.PrintWriter
import java.util.ArrayList
import java.util.Date
import java.util.HashMap
import scala.collection.JavaConverters._
import org.springframework.boot._
import org.springframework.boot.autoconfigure._
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation._
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.validation.Valid
import org.springframework.http.HttpStatus

/**
 * @author Dhwani Shah
 */

object Controller {

  def main(args: Array[String]) {
    SpringApplication.run(classOf[Controller])
  }
}

@Controller
@EnableAutoConfiguration
@RequestMapping(value=Array("/api/v1"))
class Controller {

  var moderatorList = new ArrayList[Moderator]()
  var validUser = new HashMap[String,String]()
  validUser.put("foo:bar", "authorized")

  @RequestMapping(value=Array("/moderators"),method=Array(RequestMethod.POST),headers = Array("Accept=application/json"))
  @ResponseBody
  def createModerator(@RequestBody @Valid moderator: Moderator, bindingResult: BindingResult, httpResponse: HttpServletResponse, httpRequest: HttpServletRequest): Any = {
    val msg:String = validate(bindingResult,"Error while generating new moderator : \n")
    if(msg equals "") {
      moderator.setId(Math.round(Math.random() * (999999 - 100000 + 1) + 100000).toInt)
      moderator.setCreatedAt(new Date())
      moderatorList.add(moderator)
      
      httpResponse.setStatus(HttpServletResponse.SC_CREATED)
          return "{\"id\" : \"" + moderator.id + "\", \"name\" : \"" + moderator.name + "\", \"email\" : \"" + moderator.email + "\", \"password\" : \"" + moderator.password + "\", \"created_at\" : \"" + moderator.created_at + "\"}"
    }else{
      httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST)
      return msg
    }
  }

  @RequestMapping(value=Array("/moderators/{id}") ,method=Array(RequestMethod.GET))
  @ResponseBody
  def viewModerator(@PathVariable id:String, httpRequest : HttpServletRequest, httpResponse: HttpServletResponse): Any = {
    doHttpBasicAuthentication(httpRequest, httpResponse);
    val iterator = moderatorList.iterator()
    while (iterator.hasNext()) {
      val moderatorInfo: Moderator = iterator.next()
      val newEtag: String = generateEtag(moderatorInfo)
      val oldEtag: String = httpRequest.getHeader("ETag")
      if (newEtag == oldEtag) {
        httpResponse.setStatus(HttpServletResponse.SC_NOT_MODIFIED)
        return ""
      } else {
        if ((moderatorInfo.id.toString()).equals(id)) {
          httpResponse.setHeader("ETag",newEtag)
          httpResponse.setStatus(HttpServletResponse.SC_OK)
          return "{\"id\" : \"" + moderatorInfo.id + "\", \"name\": \"" + moderatorInfo.name + "\", \"email\" : \"" + moderatorInfo.email + "\", \"password\" : \"" + moderatorInfo.password + "\",\"created_at\" : \"" + moderatorInfo.created_at + "\"}"
        }
      }
    }
    httpResponse.setStatus(HttpServletResponse.SC_OK)
    "No moderator found"
  }

  @RequestMapping(value=Array("/moderators/{id}") ,method=Array(RequestMethod.PUT), headers = Array("Accept=application/json"))
  @ResponseBody
  def updateModerator(@PathVariable id:String , @RequestBody moderator: Moderator, bindingResult: BindingResult, httpRequest : HttpServletRequest, httpResponse: HttpServletResponse): Any = {
    doHttpBasicAuthentication(httpRequest, httpResponse);
    val msg:String = validate(bindingResult,"Error while updating moderator : \n")
    if(msg equals "") {
      val iterator = moderatorList.iterator()
      while (iterator.hasNext()) {
        val moderatorInfo: Moderator = iterator.next()
        if ((moderatorInfo.id.toString()).equals(id)) {
          if(moderator.name!=null){
            moderatorInfo.setName(moderator.name)
          }
          moderatorInfo.setEmail(moderator.email)
          moderatorInfo.setPassword(moderator.password)
          moderatorInfo.setUpdatedAt(new Date())
          val newEtag:String = generateEtag(moderator)
          httpResponse.setHeader("ETag",newEtag)
          httpResponse.setStatus(HttpServletResponse.SC_CREATED)
          return "{\"id\" : \"" + moderatorInfo.id + "\", \"name\": \""+ moderatorInfo.name +"\" ,\"email\" : \"" + moderatorInfo.email + "\", \"password\" : \"" + moderatorInfo.password + "\", \"created_at\" : \"" + moderatorInfo.created_at + "\"}"
        }
      }
      httpResponse.setStatus(HttpServletResponse.SC_NOT_FOUND)
      "No moderator found"
    }else{
      httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST)
      return msg
    }
  }

  @RequestMapping(value=Array("/moderators/{id}/polls") ,method=Array(RequestMethod.POST),headers = Array("Accept=application/json"))
  @ResponseBody
  def createPoll(@PathVariable id:String , @RequestBody @Valid poll: Poll, bindingResult: BindingResult,httpRequest : HttpServletRequest, httpResponse: HttpServletResponse): Any = {
    doHttpBasicAuthentication(httpRequest, httpResponse);
    val msg:String = validate(bindingResult,"Error while generating polls : ")
    if(msg equals "") {
    val iterator = moderatorList.iterator()
    while (iterator.hasNext()) {
      val moderatorInfo: Moderator = iterator.next()
      if ((moderatorInfo.id.toString()).equals(id)) {
         var randomNo : Int = Math.round(Math.random() * (999999 - 100000 + 1) + 100000).toInt
          poll.id = Integer.toString(randomNo, 36).toUpperCase()
          val pollResult : ArrayList[Integer] = new ArrayList[Integer]
          var choiceList = poll.choice
          for(i <- 0 to choiceList.size()-1){
            pollResult.add(i, 0)
          }
          poll.setResults(pollResult)
          moderatorInfo.pollList.add(poll)
          httpResponse.setStatus(HttpServletResponse.SC_CREATED)
          return "{ \"id\" : \"" + poll.id + "\", \"question\" : \"" + poll.question + "\", \"started_at\" : \"" + poll.started_at + "\", \"expired_at\" : \"" + poll.expired_at + "\", \"choice\" : \"" + poll.choice + "\"}"
      }
    }
     httpResponse.setStatus(HttpServletResponse.SC_OK)
    "Poll could not be created"
    }else{
      httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST)
      return msg
    }
  }
  
  @RequestMapping(value=Array("/polls/{poll_id}") ,method=Array(RequestMethod.GET))
  @ResponseBody
  def viewPoll(@PathVariable poll_id:String, httpRequest : HttpServletRequest, httpResponse: HttpServletResponse): Any = {
    val iterator = moderatorList.iterator()
    while (iterator.hasNext()) {
      val moderatorInfo: Moderator = iterator.next()
      val pollIterator = moderatorInfo.getPollList().iterator()
      while (pollIterator.hasNext()){
          val pollInfo: Poll = pollIterator.next()
          if ((pollInfo.id).equals(poll_id.toUpperCase())) {
          return "{\"id\" : \"" + pollInfo.id + "\", \"question\": \"" + pollInfo.question + "\", \"started_at\" : \"" + pollInfo.started_at + "\", \"expired_at\" : \"" + pollInfo.expired_at + "\",\"choice\" : \"" + pollInfo.choice + "\"}"
        }
      }
    }
    //httpResponse.setStatus(HttpServletResponse.SC_OK)
    "No poll found"
  }
  
  @RequestMapping(value=Array("moderators/{moderator_id}/polls/{poll_id}") ,method=Array(RequestMethod.GET))
  @ResponseBody
  def viewPollResult(@PathVariable moderator_id:String, @PathVariable poll_id:String, httpRequest : HttpServletRequest, httpResponse: HttpServletResponse): Any = {
    doHttpBasicAuthentication(httpRequest, httpResponse);
    val iterator = moderatorList.iterator()
    while (iterator.hasNext()) {
      val moderatorInfo: Moderator = iterator.next()
      if(moderatorInfo.id.toString().equals(moderator_id)){
          val pollIterator = moderatorInfo.getPollList().iterator()
          while (pollIterator.hasNext()){
          val pollInfo: Poll = pollIterator.next()
          if ((pollInfo.id).equals(poll_id.toUpperCase())) {
          return "{\"id\" : \"" + pollInfo.id + "\", \"question\": \"" + pollInfo.question + "\", \"started_at\" : \"" + pollInfo.started_at + "\", \"expired_at\" : \"" + pollInfo.expired_at + "\",\"choice\" : \"" + pollInfo.choice + "\", \"results\" : \"" + pollInfo.results + "\"}"
        }
      }
      }else{
        httpResponse.setStatus(HttpServletResponse.SC_OK)
        return "Please enter correct moderator ID"
      }
    }
    httpResponse.setStatus(HttpServletResponse.SC_OK)
    "No polls available"
  }
  
  @RequestMapping(value=Array("moderators/{moderator_id}/polls") ,method=Array(RequestMethod.GET))
  @ResponseBody
  def viewAllPolls(@PathVariable moderator_id:String, httpRequest : HttpServletRequest, httpResponse: HttpServletResponse): Any = {
    doHttpBasicAuthentication(httpRequest, httpResponse);
    val iterator = moderatorList.iterator()
    val pollDetails = new StringBuffer()
    while (iterator.hasNext()) {
      val moderatorInfo: Moderator = iterator.next()
      if(moderatorInfo.id.toString().equals(moderator_id)){
          val pollIterator = moderatorInfo.getPollList().iterator()
          var count:Int = 0
          while (pollIterator.hasNext()){
          val pollInfo: Poll = pollIterator.next()
          httpResponse.setStatus(HttpServletResponse.SC_OK)
          if(count==0){
            pollDetails.append("{\"id\" : \"" + pollInfo.id + "\", \"question\": \"" + pollInfo.question + "\", \"started_at\" : \"" + pollInfo.started_at + "\", \"expired_at\" : \"" + pollInfo.expired_at + "\",\"choice\" : \"" + pollInfo.choice + "\", \"results\" : \"" + pollInfo.results + "\"}")
          }else{
            pollDetails.append(", {\"id\" : \"" + pollInfo.id + "\", \"question\": \"" + pollInfo.question + "\", \"started_at\" : \"" + pollInfo.started_at + "\", \"expired_at\" : \"" + pollInfo.expired_at + "\",\"choice\" : \"" + pollInfo.choice + "\", \"results\" : \"" + pollInfo.results + "\"}")
          }
          count = count + 1
      }
       if(count==0){
         return "Polls not available"
      } else if(count==1) {
        return pollDetails.toString()
      } else {
        return "[" + pollDetails.toString() + "]"
      }
      }else{
        httpResponse.setStatus(HttpServletResponse.SC_OK)
        return "Please enter correct moderator ID"
      }
    }
    httpResponse.setStatus(HttpServletResponse.SC_OK)
    "No moderators available"
  }
   
  @RequestMapping(value=Array("/moderators/{id}/polls/{poll_id}") ,method=Array(RequestMethod.DELETE))
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ResponseBody
  def deletePoll(@PathVariable id:String , @PathVariable poll_id:String): String = {
    val iterator = moderatorList.iterator()
    while (iterator.hasNext()) {
      val moderatorInfo: Moderator = iterator next()
      if ((moderatorInfo.id.toString()).equals(id)) {
        val pollIterator = moderatorInfo.pollList.iterator()
        while(pollIterator.hasNext){
          val poll: Poll = pollIterator.next()
          if((poll.id.toString()).equals(poll_id.toUpperCase())){
          moderatorInfo.pollList.remove(poll)
            return "Poll removed"
          }
        }
        return "No poll available"
      }
    }
    "No moderators available"
  }
  
  @RequestMapping(value=Array("/polls/{poll_id}") ,method=Array(RequestMethod.PUT))
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ResponseBody
  def votePoll(@PathVariable poll_id:String, @RequestParam("choice") choice_index:Int): Any = {
    val iterator = moderatorList.iterator()
    while (iterator.hasNext()) {
      val moderatorInfo: Moderator = iterator next()
      val pollIterator = moderatorInfo.pollList.iterator()
      while(pollIterator.hasNext){
        val poll: Poll = pollIterator.next()
        if((poll.id.toString()).equals(poll_id.toUpperCase())){
          val pollResult : ArrayList[Integer] = poll.getResults()
          if(pollResult!=null && !pollResult.isEmpty()){
            val result : Integer = pollResult.get(choice_index)
            pollResult.set(choice_index, result+1)
           }
            return "Pole voted"
         }
      }
        return "No poll available"
    }
    "No moderators available"
    }

  def validate(bindingResult: BindingResult, message : String): String = {
    var errorMsg: String=""
    errorMsg+=message
    if(bindingResult.hasErrors()){
       for(error <- bindingResult.getFieldErrors().asScala){
         errorMsg+=error.getField + " - " + "is required" + "\n"
       }
      return errorMsg.toString()
    }else{
      ""
    }
  }

  def generateEtag(moderator: Moderator): String = {
    if(moderator.updated_at == null){
      return "GETrequest"
    }else{
      return moderator.updated_at.toString
    }
  }
  
  def allowUserLogin(auth: String): Boolean = {
    if(auth==null){
      return false
    }
    if(!auth.toUpperCase().startsWith("BASIC")){
      return false
    }
    var encodedUserPassword: String = auth.substring(6);
    var decoder: sun.misc.BASE64Decoder = new sun.misc.BASE64Decoder()
    var decodedUserPassword: String = new String(decoder.decodeBuffer(encodedUserPassword))
    if ("authorized".equals(validUser.get(decodedUserPassword))) {
       return true;
    } else {
       return false;
    }
  }
  
  def doHttpBasicAuthentication(httpRequest: HttpServletRequest, httpResponse: HttpServletResponse) = {
        var authorized: String = httpRequest.getHeader("Authorization") 
        if(!allowUserLogin(authorized)){
          httpResponse.setHeader("WWW-Authenticate", "BASIC realm=\"Provide credentials\"")
          httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED)
        }else{
          System.out.println("Permission granted")
        }
    }
}