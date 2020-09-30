from odoo import models, fields


class MyModel(models.Model):
    _inherit = 'inherited'

    my_field = fields.Many2one(related='e<caret>xisting', readonly=False)