from odoo import models


class ModelNameInInherit(models.Model):
    _name = 'model.name.in.inherit'
    _inherit = 'single_wild<caret>'
