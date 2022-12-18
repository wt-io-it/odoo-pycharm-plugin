from odoo import models


class ModelInRecursive2Module(models.Model):

    _inherit = "recursion.recursive"
