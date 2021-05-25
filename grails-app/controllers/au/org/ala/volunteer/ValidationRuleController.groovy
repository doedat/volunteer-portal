package au.org.ala.volunteer

class ValidationRuleController {

    def userService

    def index() {
        redirect(action:list())
    }

    boolean checkAdmin() {
        if(!userService.isAdmin()) {
            flash.message = "You do not have permission to view this page"
            redirect(url: "/")
            return false
        }
        return true
    }

    def list() {
        if (!checkAdmin()) {
            return
        }
        def validationRules = ValidationRule.list(params)
        [validationRules: validationRules, totalCount: ValidationRule.count ]
    }

    def delete() {
        if (!checkAdmin()) {
            return
        }
        def rule = ValidationRule.get(params.int("id"));
        if (rule) {
            rule.delete()
            flash.message = message(code: 'validationRule.rule_x_deleted', args: [rule.name])
        } else {
            flash.message = message(code: 'validationRule.no_rule_exists', args: [params.id])
        }
        redirect(action:"list")
    }

    def addRule() {
        if (!checkAdmin()) {
            return
        }
        def rule = new ValidationRule(name:"<New rule>")
        render(view: 'edit', model: [rule: rule])
    }

    def edit() {
        if (!checkAdmin()) {
            return
        }
        def rule = ValidationRule.get(params.int("id"))
        [rule: rule]
    }

    def update() {
        if (!checkAdmin()) {
            return
        }
        def rule = ValidationRule.get(params.int("id"))
        if (rule) {
            rule.properties = params
        } else {
            rule = new ValidationRule(params)
        }

        if (!rule.save()) {
            render(view:'edit', model:[rule: rule])
            return
        }

        redirect(action:'list')
    }

}
