from odoo import models, fields


class MyModel(models.Model):
    _inherit = 'inherited'

    my_field = fields.Many2many(<caret>)