from odoo import models


class MyModel(models.Model):
    _inherit = 'inherited'

    def do_action(self):
        wildcard_obj = self.env['mixed_wild<caret>card.fallback']
        return wildcard_obj
