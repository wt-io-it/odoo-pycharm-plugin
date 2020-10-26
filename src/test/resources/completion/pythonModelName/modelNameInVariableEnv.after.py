from odoo import models


class ModelNameInInherit(models.Model):
    _name = 'model.name.in.inherit'

    def _get_something(self):
        existing_model = "existing"
        self.env[existing_model]
