from odoo import models


class MyModel(models.Model):
    _inherit = 'inherited'

    def do_action(self):
        wildcard_obj = self.env['wild<caret>card.something']
        return wildcard_obj
