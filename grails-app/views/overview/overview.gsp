<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="au.org.ala.volunteer.TaskFilter; au.org.ala.volunteer.Project; au.org.ala.volunteer.Task;" %>

<html xmlns="http://www.w3.org/1999/html">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="${grailsApplication.config.ala.skin}"/>
    <g:set var="entityName" value="${message(code: 'project.label', default: 'Project')}"/>
    <title><cl:pageTitle title="${g.message(code: "default.list.label", args: ['Expedition'])}"/></title>
    <asset:stylesheet src="digivol-image-resize"/>
    <asset:stylesheet src="image-viewer"/>
    <asset:stylesheet src="overview.css"/>
    <asset:stylesheet src="loading"/>
</head>

<body class="digivol">

<cl:headerContent title="${project?.i18nName}" crumbLabel="${message(code: 'overview.overview')}">
    <%pageScope.crumbs = [[link: createLink(controller: 'project', action: 'index', params: [id: params.id]), label: project?.i18nName]] %>
</cl:headerContent>

<section id="main-content">
    <div class="container">
        <div class="row" style="padding-bottom: 10px;">
            <div class="col-sm-12">
                <g:set var="activeFilter" value="${params.activeFilter ?: TaskFilter.showReadyForTranscription}"/>
                <g:set var="urlParams"
                       value="${[sort: params.sort ?: "", order: params.order ?: "", offset: 0, q: params.q ?: "", mode: params.mode ?: "", activeFilter: activeFilter]}"/>

                <div class="btn-group pull-right" style="padding-right: 10px">
                    <g:each in="${TaskFilter.values()}" var="mode">
                        <g:set var="href" value="?${(urlParams + [activeFilter: mode]).collect { it }.join('&')}"/>
                        <a href="${href}"
                           class="btn btn-warning btn-small ${activeFilter.toString() == mode?.toString() ? "active" : ""}">${message(code: mode.i18nLabel)}</a>
                    </g:each>
                </div>
            </div>
        </div>

        <div class="row">
            <div class="col-sm-12">
                <div class="row">
                    <g:each in="${tasks}" status="i" var="task">
                        <g:render template="taskCard"
                                  model="[task: task, project: project, userId: userId, isAdmin: isAdmin]"/>
                    </g:each>
                    <div class="pagination">
                        <g:paginate total="${tasksAmount}" prev="" next=""
                                    action="showProjectOverview"
                                    id="${params.id}"/>
                    </div>
                </div>
            </div>
        </div>
    </div>
</section>
<asset:javascript src="image-viewer.js" asset-defer=""/>
<asset:javascript src="overview.js" asset-defer=""/>
</body>
</html>