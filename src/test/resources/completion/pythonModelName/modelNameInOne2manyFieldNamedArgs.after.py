from odoo import models, fields


class ModelNameInFields(models.Model):
    my_field = fields.One2many(help='my help text', comodel_name='existing')
