from odoo import models, fields


class ModelNameInFields(models.Model):
    # second argument should not be autocompleted by model name
    my_field = fields.One2many('existing', 'exi')
