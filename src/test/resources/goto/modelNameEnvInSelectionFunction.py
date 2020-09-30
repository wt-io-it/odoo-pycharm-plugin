from odoo import models, fields, api

@api.model
def _my_selection(self):
    return self.env['e<caret>xisting'].compute_something()

class MyModel(models.Model):
    _inherit = 'inherited'

    my_field = fields.Selection(_my_selection)
