from odoo import models, api, SUPERUSER_ID

class ModelNameInInherit(models.Model):
    _name = 'model.name.in.inherit'

    def _get_something(self):
        env = api.Environment(self.env.cr, SUPERUSER_ID, {})
        env.ref("addon1.autocomplete_target_record")
