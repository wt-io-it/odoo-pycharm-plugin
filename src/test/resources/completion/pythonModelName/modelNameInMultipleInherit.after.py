from odoo import models


class Model1(models.Model):
    _name = 'model.name.in.multiple.inherit.model1'


class ModelNameInInherit(models.Model):
    _name = 'model.name.in.multiple.inherit'
    _inherit = ['model.name.in.multiple.inherit.model1', 'existing']
