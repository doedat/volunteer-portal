<%@ page import="au.org.ala.volunteer.DateConstants" %>
This message was automatically generated by <g:message code="default.application.name" />. Please do not reply to this message directly.

New messages have been added to one or more of the forum topics that you are currently watching:

<g:each in="${messages}" var="message" status="messageNo">

  Message: ${messageNo+1}
  Topic: ${message.topic.title} [ ${createLink(controller:'forum', action:'viewForumTopic', id:message.topic.id, absolute: true)} ]
  On ${formatDate(date: message.date, format: DateConstants.DATE_TIME_FORMAT)}, ${message.user.displayName} wrote:

  ${message.text}

</g:each>



