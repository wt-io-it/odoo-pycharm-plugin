from odoo import models


class ModelB(models.Model):
    _inherit = 'not_model_b'
    _name = "model_b"
