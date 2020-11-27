from odoo import models, fields


class Inherited(models.Model):
    _inherit = 'inherited'

    name = fields.Char('Name')

class Interited2(models.Model):
    _name = 'inherited2'
    _inherit = 'inherited'

class Interited3(models.Model):
    _name = 'inherited3'
    _inherit = 'inherited'

    code = fields.Char('code')