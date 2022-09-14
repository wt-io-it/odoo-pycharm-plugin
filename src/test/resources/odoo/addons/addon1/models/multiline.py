from odoo import models


class ModelWithMultilineName(models.Model):
    _name = "model_with_name_in_" \
            "multiple_lines"