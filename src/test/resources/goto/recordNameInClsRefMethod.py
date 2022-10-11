from odoo.models import Model


class MyModel(Model):

    @classmethod
    def action_test_goto(cls):
        cls.env.ref('addon1.rec<caret>ord1')