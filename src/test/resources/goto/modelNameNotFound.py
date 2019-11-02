from odoo import models


class MyModel(models.Model):
    _name = 'not<caret>existing'