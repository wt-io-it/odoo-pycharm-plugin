from odoo import models


class MyModel(models.Model):
    _inherit = 'inherited'

    def do_action(self):
        existing_obj = self.env['e<caret>xisting']
        return existing_obj
