package au.org.ala.volunteer

import au.org.ala.web.AlaSecured
import grails.converters.JSON
import grails.transaction.Transactional

@AlaSecured("ROLE_VP_ADMIN")
class LeaderBoardAdminController {

    def settingsService
    def userService
    def authService
    
    def index() {
        def ids = settingsService.getSetting(SettingDefinition.IneligibleLeaderBoardUsers)
        [users: ids ? userService.detailsForUserIds(ids) : []]
    }

    boolean checkAdmin() {
        if(!userService.isAdmin()) {
            flash.message = "You do not have permission to view this page"
            redirect(url: "/")
            return false
        }
        return true
    }

    def findEligibleUsers(String term) {
        if (!checkAdmin()) {
            return
        }
        // todo search Atlas User Details
        def ineligible = settingsService.getSetting(SettingDefinition.IneligibleLeaderBoardUsers) ?: []
        def search = "%${term}%"
        def users = User.withCriteria {
            or {
                ilike 'displayName', search
                ilike 'email', search
            }
            if (ineligible) {
                not {
                    inList 'userId', ineligible
                }
            }
            maxResults 20
            order "displayName", "desc"
        }

        render users as JSON
    }

    def addIneligibleUser(int id) {
        if (!checkAdmin()) {
            return
        }
        settingsService.setSetting(SettingDefinition.IneligibleLeaderBoardUsers.key, 
                settingsService.getSetting(SettingDefinition.IneligibleLeaderBoardUsers) + (Integer.toString(id)))
        render status: 204
    }

    def removeIneligibleUser(int id) {
        if (!checkAdmin()) {
            return
        }
        settingsService.setSetting(SettingDefinition.IneligibleLeaderBoardUsers.key,
                settingsService.getSetting(SettingDefinition.IneligibleLeaderBoardUsers) - (Integer.toString(id)))
        render status: 204
    }
}
