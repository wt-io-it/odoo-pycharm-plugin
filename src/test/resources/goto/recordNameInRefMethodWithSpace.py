from odoo.models import Model


class MyModel(Model):

    def action_test_goto(self):
        self.env.ref ('addon1.rec<caret>ord1')