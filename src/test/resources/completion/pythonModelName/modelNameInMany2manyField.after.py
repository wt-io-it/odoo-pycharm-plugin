from odoo import models, fields


class ModelNameInFields(models.Model):
    my_field = fields.Many2many('existing')
