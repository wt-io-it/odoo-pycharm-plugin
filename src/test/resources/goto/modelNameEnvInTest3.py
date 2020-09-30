from odoo.tests import SingleTransactionCase


class MyTest(SingleTransactionCase):

    def test_something(self):
        existing_obj = self.env['e<caret>xisting']
        existing_obj.do_something()
