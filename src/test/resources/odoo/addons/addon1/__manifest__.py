{
    'depends': [
        'addon2',
        'not_existing_addon',
    ],
    'data': [
        'data/existing.csv',
        'data/inherited.csv',
        'data/inherited2.csv',
        'data/inherited3.csv',
        'data/records.xml',
        'data/records2.xml',

        'views/existing_view.xml',
    ],
    'assets': {
        'addon1.assets_addon1': {
            'addon1/static/src/scss/variables.scss'
        }
    }
}