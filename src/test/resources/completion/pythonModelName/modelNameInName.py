from odoo import models


class ModelNameInName(models.Model):
    _name = 'exi<caret>'
