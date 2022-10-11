from odoo import models


class MyModel(models.Model):
    _inherit = 'inherited'

    def do_action(self):
        wildcard_obj = self.env['mixed_wild<caret>card.format_explicit']
        return wildcard_obj
