from odoo import models


class ModelNameInInherit(models.Model):
    _name = 'model.name.in.inherit'

    @classmethod
    def _get_something(cls):
        cls.env.ref("addon1.autocomplete_target_record")
