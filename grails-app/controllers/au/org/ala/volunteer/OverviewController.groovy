package au.org.ala.volunteer

class OverviewController {

    def userService

    def index() {
        if (params.id) {
            log.debug("index redirect to showProjectOverview: " + params.id)
            redirect(action: "showProjectOverview", id: params.id)
        } else {
            flash.message = message(code: 'transcribe.something_unexpected_happened')
            redirect(uri: "/")
        }
    }

    def showProjectOverview() {
        def project = Project.get(params.id)
        def hasOverview = project.hasOverviewPage

        if(!hasOverview) {
            redirect(controller: "project", action: "index", params: params)
        }

        def userId = userService.currentUserId
        def isAdmin = userService.isAdmin()

        if (project == null) {
            log.error("Project not found for id: " + params.id)
            redirect(view: '/index')
        }

        log.debug("project id = " + params.id + " || msg = " + params.msg + " || prevInt = " + params.prevId)

        if (params.msg) {
            flash.message = params.msg
        }

        def filter = params.activeFilter
        params.activeFilter = filter ?: TaskFilter.showReadyForTranscription
        def max = Math.min(params.max ? params.int('max') : 12, 20)
        params.max = max
        def order = params.order ?: 'asc'
        params.order = order
        def sort = params.sort ?: 'externalIdentifier'
        params.sort = sort
        def offset = (params.offset ?: 0) as int
        params.offset = offset

        def tasks = Task.findAllByProject(project)
        def filteredTasks

        switch (params.activeFilter) {
            case TaskFilter.showReadyForTranscription.toString():
                filteredTasks = tasks.findAll { it.isAvailableForTranscription(userId) }
                break
            case TaskFilter.showTranscriptionLocked.toString():
                filteredTasks = tasks.findAll { !it.isAvailableForTranscription(userId) }
                break
            default:
                filteredTasks = tasks
                break
        }

        filteredTasks.sort { it.externalIdentifier }
        def fromIndex = Math.min(offset, filteredTasks.size())
        def toIndex = Math.min(offset + max, filteredTasks.size())

        def paginatedTasks

        if(filteredTasks.size() > 0) {
            paginatedTasks = filteredTasks.subList(fromIndex, toIndex)
        } else {
            paginatedTasks = filteredTasks
        }

        render(view: 'overview', model: [
                project     : project,
                userId      : userId,
                isAdmin     : isAdmin,
                tasks       : paginatedTasks,
                tasksAmount : filteredTasks.size()
        ])
    }

    def preview() {
        def task = Task.get(params.taskId)

        render(template: "preview", model: [
                task: task,
                project: task?.project?.id,
        ])
    }

}
