<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title><g:message code="default.application.name"/> - Atlas of Living Australia</title>
    <meta name="layout" content="${grailsApplication.config.ala.skin}"/>
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'forum.css')}"/>

    <style type="text/css">

    h2 {
        padding-top: 10px;
    }

    textarea {
        width: 100%;
    }

    </style>

</head>

<body>

<r:script type="text/javascript">

            $(document).ready(function () {

                $("#btnCancel").click(function (e) {
                    e.preventDefault();
                    window.location = "${createLink(controller: 'forum', action: 'viewForumTopic', id: forumMessage?.topic?.id)}";
                });

            });

</r:script>

<cl:headerContent title="" selectedNavItem="forum" hideTitle="${true}" hideCrumbs="${true}">
    <vpf:forumNavItems topic="${forumMessage?.topic}"
                       lastLabel="${message(code: 'forum.project.editMessage', default: 'Edit Message')}"/>
</cl:headerContent>

<div class="row">
    <div class="span12">
        <h2>Your message:</h2>
        <small>* Note: To see help on how to format your messages, including bold and italics, see <a
                href="${createLink(action: 'markdownHelp')}" target="popup">here</a></small>
        <g:form id="messageForm" controller="forum">
            <g:hiddenField name="messageId" value="${forumMessage?.id}"/>
            <g:textArea id="messageText" name="messageText" rows="12" cols="120" value="${messageText}"/>
            <label for="watchTopic">
                <g:checkBox name="watchTopic" checked="${isWatched}"/>
                Watch this topic
            </label>

            <div>
                <g:actionSubmit class="btn" value="Preview" action="previewMessageEdit"/>
                <g:actionSubmit class="btn" value="Save message" action="updateTopicMessage"/>
                <button class="btn" id="btnCancel">Cancel</button>
            </div>

        </g:form>

        <g:if test="${messageText}">
            <div class="messagePreview">
                <h3>Message preview</h3>
                <markdown:renderHtml>${messageText}</markdown:renderHtml>
            </div>
        </g:if>
    </div>
</div>
</body>
</html>
