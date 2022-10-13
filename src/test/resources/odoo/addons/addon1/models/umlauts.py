from odoo import models

class AnInvalidModelWithUmlauts(models.Model):
    # there cannot be a model with umlauts in odoo
    # we test here that we still do not throw an index error if the user makes a typo
    _name = 'odoo.äöü'
