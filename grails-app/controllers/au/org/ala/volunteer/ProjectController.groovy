package au.org.ala.volunteer

import grails.converters.*
import org.imgscalr.Scalr
import org.springframework.web.multipart.MultipartHttpServletRequest
import org.springframework.web.multipart.MultipartFile
import au.org.ala.cas.util.AuthenticationCookieUtils

import javax.imageio.ImageIO

class ProjectController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    static numbers = ["Zero","One", 'Two', 'Three', 'Four', 'Five', 'Six', 'Seven', 'Eight', 'Nine', 'Ten', 'Eleven', 'Twelve', 'Thirteen', 'Fourteen', 'Fifteen', 'Sixteen', 'Seventeen', 'Eighteen', 'Nineteen', 'Twenty']

    def grailsApplication
    def taskService
    def fieldService
    def multimediaService
    def logService
    def authService
    def exportService
    def collectionEventService
    def localityService
    def projectService
    def picklistService

    /**
     * Project home page - shows stats, etc.
     */
    def index = {
        def projectInstance = Project.get(params.id)

        String currentUserId = null

        currentUserId = AuthenticationCookieUtils.getUserName(request)

        if (!projectInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'project.label', default: 'Project'), params.id])}"
            redirect(action: "list")
        } else {
            // project info
            def taskCount = Task.countByProject(projectInstance)
            def tasksTranscribed = Task.countByProjectAndFullyTranscribedByIsNotNull(projectInstance)
            def userIds = taskService.getUserIdsAndCountsForProject(projectInstance, new HashMap<String, Object>())
            def expedition = grailsApplication.config.expedition
            def roles = [] //  List of Map
            // copy expedition data structure to "roles" & add "members"
            expedition.each {
                def row = it.clone()
                row.put("members", [])
                roles.addAll(row)
            }
            
            userIds.each {
                // iterate over each user and assign to a role.
                def userId = it[0]
                def count = it[1]
                def assigned = false
                def user = User.findByUserId(userId)
                if (user) {
                    roles.eachWithIndex { role, i ->
                        if (count >= role.threshold && role.members.size() < role.max && !assigned) {
                            // assign role
                            def userMap = [name: user.displayName, id: user.id, count: count, userId: user.userId]
                            role.get("members").add(userMap)
                            assigned = true
                            log.debug("assigned: " + userId)
                        } else {
                            log.debug("not assigned: " + userId)
                        }
                    }
                }
            }

            def leader = roles.find { it.name == "Expedition Leader" } ?.members.getAt(0)
            def items = projectInstance.newsItems.asList()
            def newsItem = items.size() > 0 ? items[0] : null;

            def percentComplete = (taskCount > 0) ? ((tasksTranscribed / taskCount) * 100) : 0
            if (percentComplete > 99 && taskCount != tasksTranscribed) {
                // Avoid reporting 100% unless the transcribed count actually equals the task count
                percentComplete = 99;
            }

            render(view: "index", model: [projectInstance: projectInstance, taskCount: taskCount, tasksTranscribed: tasksTranscribed, roles:roles, newsItem: newsItem, currentUserId: currentUserId, leader: leader, percentComplete: percentComplete])
        }
    }

    /**
     * REST web service to return a list of tasks with coordinates to show on Google Map
     */
    def tasksToMap = {
        def projectInstance = Project.get(params.id)
        def taskListFields = []

        if (projectInstance) {
            long startQ  = System.currentTimeMillis();
            def taskList = Task.findAllByProjectAndFullyTranscribedByIsNotNull(projectInstance, [sort:"id", max:999])

            if (taskList.size() > 0) {
                def lats = fieldListToMap(fieldService.getLatestFieldsWithTasks("decimalLatitude", taskList, params))
                def lngs = fieldListToMap(fieldService.getLatestFieldsWithTasks("decimalLongitude", taskList, params))
                long endQ  = System.currentTimeMillis();
                log.debug("DB query took " + (endQ - startQ) + " ms")
                log.debug("List sizes: task = " + taskList.size() + "; lats = " + lats.size() + "; lngs = " + lngs.size())
                taskList.eachWithIndex { tsk, i ->
                    def jsonObj = [:]
                    jsonObj.put("id",tsk.id)

                    if (lats.containsKey(tsk.id) && lngs.containsKey(tsk.id)) {
                        jsonObj.put("lat",lats.get(tsk.id))
                        jsonObj.put("lng",lngs.get(tsk.id))
                        taskListFields.add(jsonObj)
                    }
                }

                long endJ  = System.currentTimeMillis();
                log.debug("JSON loop took " + (endJ - endQ) + " ms")
                log.debug("Method took " + (endJ - startQ) + " ms for " + taskList.size() + " records")
            }
            render taskListFields as JSON
        } else {
            // no project found
            render("No project found for id: " + params.id) as JSON
        }
    }

    /**
     * Output list of email addresses for a given project
     */
    def mailingList = {
        def projectInstance = Project.get(params.id)

        if (projectInstance && authService.userInRole(CASRoles.ROLE_ADMIN)) {
            def userIds = taskService.getUserIdsForProject(projectInstance)
            log.debug("userIds = " + userIds)
            //render(userIds)
            def list = userIds.join(";\n")
            render(text:list, contentType: "text/plain")
        }
        else if (projectInstance) {
            render("You do not have permission to access this page.")
        }
        else {
            render("No project found for id: " + params.id)
        }
    }

    /**
     * Utility to convert list of Fields to a Map with task.id as key
     *
     * @param fieldList
     * @return
     */
    private Map fieldListToMap(List fieldList) {
        Map fieldMap = [:]
        fieldList.each {
            if (it.value) {
                fieldMap.put(it.task.id, it.value)
            }
        }

        return fieldMap
    }

    /**
     * Utility to convert list of Fields to a Map of Maps with task.id as key
     *
     * @param fieldList
     * @return
     */
    private static Map fieldListToMultiMap(List fieldList) {
        Map taskMap = [:]

        fieldList.each {
            if (it.value) {
                Map fm = null;

                if (taskMap.containsKey(it.task.id)) {
                    fm = taskMap.get(it.task.id)
                } else {
                    fm = [:]
                    taskMap[it.task.id] = fm
                }

                Map valueMap = null;
                if (fm.containsKey(it.name)) {
                   valueMap = fm[it.name]
                } else {
                    valueMap = [:]
                    fm[it.name] = valueMap
                }

                valueMap[it.recordIdx] = it.value
            }
        }

        return taskMap
    }

    /**
     * Produce an export file
     */
    def exportCSV = {
        def projectInstance = Project.get(params.id)
        boolean transcribedOnly = params.transcribed?.toBoolean()
        boolean validatedOnly = params.validated?.toBoolean()

        if (projectInstance) {
            def taskList
            if (transcribedOnly) {
                taskList = Task.findAllByProjectAndFullyTranscribedByIsNotNull(projectInstance, [sort:"id", max:9999])
            } else if (validatedOnly) {
                taskList = Task.findAllByProjectAndIsValid(projectInstance, true, [sort:"id", max:9999])
            } else {
                taskList = Task.findAllByProject(projectInstance, [sort:"id", max:9999])
            }
            def taskMap = fieldListToMultiMap(fieldService.getAllFieldsWithTasks(taskList))
            def fieldNames =  ["taskID", "transcriberID", "validatorID", "externalIdentifier", "exportComment", "dateTranscribed", "dateValidated"]
            fieldNames.addAll(fieldService.getAllFieldNames(taskList))

            Closure export_func = exportService.export_default
            if (params.exportFormat == 'zip') {
                export_func = exportService.export_zipFile
            }

//            def exporter_func_property = exportService.metaClass.getProperties().find() { it.name == 'export_' + projectInstance.template.name }
//            if (exporter_func_property) {
//                export_func = exporter_func_property.getProperty(exportService)
//            }

            if (export_func) {
                response.setHeader("Cache-Control", "must-revalidate");
                response.setHeader("Pragma", "must-revalidate");
                export_func(projectInstance, taskList, taskMap, fieldNames, response)
            } else {
                throw new Exception("No export function for template ${projectInstance.template.name}!")
            }

        }
        else {
            throw new Exception("No project found for id: " + params.id)
        }
    }

    def deleteTasks = {

        def projectInstance = Project.get(params.id)
        boolean deleteImages = params.deleteImages?.toBoolean()
        if (projectInstance) {
            def tasks = Task.findAllByProject(projectInstance)
            for (Task t : tasks) {
                try {
                    if (deleteImages) {
                        t.multimedia.each { image ->
                            try {
                                multimediaService.deleteMultimedia(image)
                            } catch (IOException ex) {
                                logService.log("Failed to delete multimedia: " + ex.message)
                            }
                        }
                    }
                    t.delete()
                } catch (Exception ex) {
                    logService.log("Failed to delete task ${t.id}: " + ex.message)
                }
            }
        }
        redirect(action: "edit", id: projectInstance?.id)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 24, 1000)

        params.sort = params.sort ?: session.expeditionSort ? session.expeditionSort : 'completed'

        def projectSummaryList = projectService.getProjectSummaryList(params)

        def numberOfUncompletedProjects = projectSummaryList.numberOfIncompleteProjects < numbers.size() ? numbers[projectSummaryList.numberOfIncompleteProjects] : "" + projectSummaryList.numberOfIncompleteProjects;

        session.expeditionSort = params.sort;

        [
            projects: projectSummaryList.projectRenderList,
            projectInstanceTotal: projectSummaryList.matchingProjectCount,
            numberOfUncompletedProjects: numberOfUncompletedProjects
        ]
    }

    def create = {
        def currentUser = authService.username()
        if (currentUser != null && authService.userInRole(CASRoles.ROLE_ADMIN)) {
            def projectInstance = new Project()
            projectInstance.properties = params

            def eventCollectionCodes = [""]
            eventCollectionCodes.addAll(collectionEventService.getCollectionCodes())

            def localityCollectionCodes = [""]
            localityCollectionCodes.addAll(localityService.getCollectionCodes())

            def picklistInstitutionCodes = [""]
            picklistInstitutionCodes.addAll(picklistService.getInstitutionCodes())

            return [projectInstance: projectInstance, templateList: Template.list(), eventCollectionCodes: eventCollectionCodes, localityCollectionCodes: localityCollectionCodes, picklistInstitutionCodes: picklistInstitutionCodes]
        } else {
            flash.message = "You do not have permission to view this page (${CASRoles.ROLE_ADMIN} required)"
            redirect(controller: "project", action: "index", id: params.id)
        }
    }

    def save = {
        def projectInstance = new Project(params)

        if (!projectInstance.template) {
            flash.message = "Please select a template before continuing!"
            render(view: "create", model: [projectInstance: projectInstance])
            return
        }

        if (!projectInstance.featuredLabel) {
            flash.message = "You must supply a featured label!"
            render(view: "create", model: [projectInstance: projectInstance])
            return
        }

        if (projectInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'project.label', default: 'Project'), projectInstance.id])}"
            redirect(action: "index", id: projectInstance.id)
        } else {
            render(view: "create", model: [projectInstance: projectInstance])
        }

    }

    /**
     * Redirects a image for the supplied project
     */
    def showImage = {
        def projectInstance = Project.get(params.id)
        if (projectInstance) {
            params.max = 1
            def task = Task.findByProject(projectInstance, params)
            if (task?.multimedia?.filePathToThumbnail) {
                redirect(url: grailsApplication.config.server.url + task?.multimedia?.filePathToThumbnail.get(0))
            }
        }
    }

    def show = {
        def projectInstance = Project.get(params.id)
        if (!projectInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'project.label', default: 'Project'), params.id])}"
            redirect(action: "list")
        } else {
            redirect(action:'index', id: projectInstance.id)
        }
    }

    def edit = {

        def currentUser = authService.username()
        if (currentUser != null && authService.userInRole(CASRoles.ROLE_ADMIN)) {
            def projectInstance = Project.get(params.int("id"))
            if (!projectInstance) {
                flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'project.label', default: 'Project'), params.id])}"
                redirect(action: "list")
            } else {

                def picklistInstitutionCodes = [""]
                picklistInstitutionCodes.addAll(picklistService.getInstitutionCodes())

                def taskCount = Task.countByProject(projectInstance)

                return [projectInstance: projectInstance, taskCount: taskCount, picklistInstitutionCodes: picklistInstitutionCodes ]
            }
        } else {
            flash.message = "You do not have permission to view this page (${CASRoles.ROLE_ADMIN} required)"
            redirect(controller: "project", action: "index", id: params.id)
        }
    }

    def update = {
        def projectInstance = Project.get(params.id)
        if (projectInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (projectInstance.version > version) {

                    projectInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'project.label', default: 'Project')] as Object[], "Another user has updated this Project while you were editing")
                    render(view: "edit", model: [projectInstance: projectInstance])
                    return
                }
            }
            projectInstance.properties = params
            if (!projectInstance.hasErrors() && projectInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'project.label', default: 'Project'), projectInstance.id])}"
                redirect(action: "index", id: projectInstance.id)
            }
            else {
                render(view: "edit", model: [projectInstance: projectInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'project.label', default: 'Project'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def projectInstance = Project.get(params.id)
        if (projectInstance) {
            try {
                projectService.deleteProject(projectInstance)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'project.label', default: 'Project'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'project.label', default: 'Project'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'project.label', default: 'Project'), params.id])}"
            redirect(action: "list")
        }
    }
    
    def uploadFeaturedImage = {
        def projectInstance = Project.get(params.id)

        if(request instanceof MultipartHttpServletRequest) {
            MultipartFile f = ((MultipartHttpServletRequest) request).getFile('featuredImage')
            
            if (f != null) {
                def allowedMimeTypes = ['image/jpeg', 'image/png']
                if (!allowedMimeTypes.contains(f.getContentType())) {
                    flash.message = "Image must be one of: ${allowedMimeTypes}"
                    render(view:'edit', model:[projectInstance:projectInstance])
                    return;
                }

                try {
                    def filePath = "${grailsApplication.config.images.home}/project/${projectInstance.id}/expedition-image.jpg"
                    def file = new File(filePath);
                    file.getParentFile().mkdirs();
                    f.transferTo(file);
                    checkAndResizeExpeditionImage(projectInstance)
                } catch (Exception ex) {
                    flash.message = "Failed to upload image: " + ex.message;
                    render(view:'edit', model:[projectInstance:projectInstance])
                    return;
                }

            }
        }
        redirect(action: "edit", id: params.id)
    }

    def resizeExpeditionImage() {
        def projectInstance = Project.get(params.int("id"))
        if (projectInstance) {
            checkAndResizeExpeditionImage(projectInstance)
        }
        redirect(action:'edit', id:projectInstance?.id)
    }

    private checkAndResizeExpeditionImage(Project projectInstance) {
        try {
            def filePath = "${grailsApplication.config.images.home}/project/${projectInstance.id}/expedition-image.jpg"
            def file = new File(filePath);
            // Now check image size...
            def image = ImageIO.read(file)
            println "Checking Featured image for project ${projectInstance.id}: Dimensions ${image.width} x ${image.height}"
            if (image.width != 254 || image.height != 158) {
                println "Image is not the correct size. Scaling to 254 x 158..."
                image = ImageUtils.scale(image, 254, 158)
                println "Saving new dimensions ${image.width} x ${image.height}"
                ImageIO.write(image, "jpg", file)
                println "Done."
            } else {
                println "Image Ok. No scaling required."
            }
            return true
        } catch (Exception ex) {
            println ex
            ex.printStackTrace()
            return false
        }
    }

    def setLeaderIconIndex = {
        if (params.id) {
            def project = Project.get(params.id)
            if (project) {
                def iconIndex = params.int("iconIndex")?:0
                def role = grailsApplication.config.expedition[0]
                def icons = role.icons
                if (iconIndex >= 0 && iconIndex < icons.size()) {
                    project.leaderIconIndex = iconIndex
                    project.save()
                }
            }
        }

        redirect(action: "index", id: params.id)
    }

    def projectLeaderIconSelectorFragment() {
        def projectInstance = Project.get(params.getInt("id"))
        def expeditionConfig = grailsApplication.config.expedition
        // find the leader role from the config map
        def role = expeditionConfig.find { it.name == "Expedition Leader"}
        [projectInstance: projectInstance, role: role]
    }

    def updateMapInitialPosition() {
        def projectInstance = Project.get(params.int("projectId"))
        if (projectInstance) {
            def zoom = params.int("mapZoomLevel")
            def latitude = params.double("mapLatitude")
            def longitude = params.double("mapLongitude")

            if (zoom && latitude && longitude) {
                projectInstance.mapInitZoomLevel = zoom
                projectInstance.mapInitLatitude = latitude
                projectInstance.mapInitLongitude = longitude
            }
        }
        redirect(action:'edit', id:projectInstance?.id)
    }

    def createNewProjectFlow = {

        welcome {
            on("continue").to "projectDetails"
            on("cancel").to "cancel"
        }

        projectDetails {
            on("continue") {
                println params.projectName
                flow.projectName = params.projectName
            }.to "summary"
            on("cancel").to "cancel"
            on("back").to "welcome"
        }

        summary {
            on("continue").to "createProject"
            on("cancel").to "cancel"
            on("back").to "projectDetails"
        }

        createProject {
            action {
                println "Create Project: ${flow.projectName}"
            }
            on("success").to "finish"
            on(Exception).to "handleError"
        }

        cancel {
            redirect(controller:'admin', action:"index")
        }

        finish {
            // TODO: redirect to project home page!
            redirect(controller:'admin', action:"index")
        }

    }

}
