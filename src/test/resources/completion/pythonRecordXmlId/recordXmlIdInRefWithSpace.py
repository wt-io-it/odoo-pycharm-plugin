from odoo import models


class ModelNameInInherit(models.Model):
    _name = 'model.name.in.inherit'

    def _get_something(self):
        self.env.ref ("addon1.auto<caret>")
